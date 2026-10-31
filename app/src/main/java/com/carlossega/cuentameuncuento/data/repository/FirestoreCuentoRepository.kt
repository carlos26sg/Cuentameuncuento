package com.carlossega.cuentameuncuento.data.repository

import android.content.Context
import android.util.Log
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LineaCuento
import com.carlossega.cuentameuncuento.di.ApplicationScope
import com.carlossega.cuentameuncuento.di.IoDispatcher
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Cuentos guardados en Firestore:
 * - `cuentos/{id}`: titulo_xxx, desc_xxx, imagen (portada), txt_xxx (URL del texto)
 * - `imagenes/{id}`: img_N (URL de cada ilustración)
 *
 * Firestore guarda una copia local de los documentos y los textos se guardan en disco.
 * Antes se esperaba siempre la respuesta del servidor aunque ya hubiera copia (4-5 s por
 * pantalla); ahora se usa la copia y se actualiza en segundo plano una vez por sesión.
 */
@Singleton
class FirestoreCuentoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val httpClient: OkHttpClient,
    @ApplicationContext context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
    @ApplicationScope private val segundoPlano: CoroutineScope
) : CuentoRepository {

    // noBackupFilesDir: se puede volver a descargar, no tiene sentido copiarlo en el backup
    private val carpetaTextos = File(context.noBackupFilesDir, "cuentos")

    // Lo que ya se ha actualizado desde el servidor en esta sesión
    private val actualizados = ConcurrentHashMap.newKeySet<String>()

    override fun cuentos(idioma: Idioma): Flow<List<Cuento>> = flow {
        val coleccion = firestore.collection(COLECCION_CUENTOS)
        val guardados = intentar { coleccion.get(Source.CACHE).await() }
            ?.takeUnless { it.isEmpty }
            ?.aCuentos(idioma)
        if (guardados != null) emit(guardados)

        val delServidor = try {
            coleccion.get(Source.SERVER).await().aCuentos(idioma)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin conexión: basta con la copia guardada, si la había
            if (guardados == null) throw e
            Log.w(TAG, "No se pudo actualizar la lista de cuentos", e)
            return@flow
        }
        if (delServidor != guardados) emit(delServidor)
    }

    override suspend fun getLineas(cuentoId: String, idioma: Idioma): Result<List<LineaCuento>> =
        resultado {
            val fichero = File(carpetaTextos, "${cuentoId}_${idioma.codigo}.txt")
            val guardado = leerDeDisco(fichero)
            val contenido = if (guardado != null) {
                actualizarUnaVez(fichero.name) { descargarTexto(cuentoId, idioma, fichero, Source.SERVER) }
                guardado
            } else {
                descargarTexto(cuentoId, idioma, fichero, Source.DEFAULT)
            }
            LineaCuento.desdeFichero(contenido)
        }

    override suspend fun getImagenes(cuentoId: String): Result<Map<String, String>> = resultado {
        val referencia = firestore.collection(COLECCION_IMAGENES).document(cuentoId)
        val guardado = intentar { referencia.get(Source.CACHE).await() }?.takeIf { it.exists() }
        val documento = if (guardado != null) {
            // Al pedirlo al servidor, Firestore actualiza su copia para la próxima vez
            actualizarUnaVez("imagenes_$cuentoId") { referencia.get(Source.SERVER).await() }
            guardado
        } else {
            referencia.get().await()
        }
        documento.data.orEmpty()
            .filterKeys { it.startsWith(PREFIJO_IMAGEN) }
            .mapNotNull { (clave, valor) ->
                (valor as? String)?.takeIf { it.isNotEmpty() }?.let { clave.removePrefix(PREFIJO_IMAGEN) to it }
            }
            .toMap()
    }

    private suspend fun descargarTexto(cuentoId: String, idioma: Idioma, fichero: File, origen: Source): String {
        val url = firestore.collection(COLECCION_CUENTOS).document(cuentoId).get(origen).await()
            .getString("txt_${idioma.codigo}")
            .orEmpty()
        if (url.isEmpty()) throw NoSuchElementException("El cuento $cuentoId no tiene texto en ${idioma.codigo}")
        return descargar(url).also { guardarEnDisco(fichero, it) }
    }

    /** Lanza [accion] en segundo plano la primera vez que se pide [clave] en esta sesión */
    private fun actualizarUnaVez(clave: String, accion: suspend () -> Unit) {
        if (!actualizados.add(clave)) return
        segundoPlano.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                actualizados.remove(clave)
                Log.w(TAG, "No se pudo actualizar $clave", e)
            }
        }
    }

    private fun QuerySnapshot.aCuentos(idioma: Idioma): List<Cuento> =
        documents.mapNotNull { it.aCuento(idioma) }.sortedBy { it.titulo.lowercase() }

    private fun DocumentSnapshot.aCuento(idioma: Idioma): Cuento? {
        val titulo = getString("titulo_${idioma.codigo}").orEmpty()
        val id = getString("id").orEmpty().ifEmpty { this.id }
        if (titulo.isEmpty()) return null
        return Cuento(
            id = id,
            titulo = titulo,
            descripcion = getString("desc_${idioma.codigo}").orEmpty(),
            imagen = getString("imagen").orEmpty()
        )
    }

    private suspend fun descargar(url: String): String = withContext(io) {
        httpClient.newCall(Request.Builder().url(url).build()).execute().use { respuesta ->
            if (!respuesta.isSuccessful) throw IOException("HTTP ${respuesta.code} al descargar el cuento")
            respuesta.body.string()
        }
    }

    private suspend fun guardarEnDisco(fichero: File, contenido: String) = withContext(io) {
        try {
            fichero.parentFile?.mkdirs()
            fichero.writeText(contenido)
        } catch (e: IOException) {
            Log.w(TAG, "No se pudo guardar el cuento en disco", e)
        }
    }

    private suspend fun leerDeDisco(fichero: File): String? = withContext(io) {
        fichero.takeIf { it.exists() }?.readText()
    }

    /** Lecturas de la copia local: si no hay copia, Firestore lanza una excepción */
    private inline fun <T> intentar(bloque: () -> T): T? = try {
        bloque()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private inline fun <T> resultado(bloque: () -> T): Result<T> = try {
        Result.success(bloque())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Error accediendo a los cuentos", e)
        Result.failure(e)
    }

    private companion object {
        const val TAG = "CuentoRepository"
        const val COLECCION_CUENTOS = "cuentos"
        const val COLECCION_IMAGENES = "imagenes"
        const val PREFIJO_IMAGEN = "img_"
    }
}
