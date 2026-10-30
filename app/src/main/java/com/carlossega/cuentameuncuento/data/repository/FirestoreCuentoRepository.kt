package com.carlossega.cuentameuncuento.data.repository

import android.content.Context
import android.util.Log
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LineaCuento
import com.carlossega.cuentameuncuento.di.IoDispatcher
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Cuentos guardados en Firestore:
 * - `cuentos/{id}`: titulo_xxx, desc_xxx, imagen (portada), txt_xxx (URL del texto)
 * - `imagenes/{id}`: img_N (URL de cada ilustración)
 *
 * Firestore ya mantiene una caché offline de los documentos. Los textos se descargan
 * por HTTPS y se guardan en disco para que los cuentos abiertos funcionen sin conexión.
 */
@Singleton
class FirestoreCuentoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val httpClient: OkHttpClient,
    @ApplicationContext context: Context,
    @IoDispatcher private val io: CoroutineDispatcher
) : CuentoRepository {

    // noBackupFilesDir: se puede volver a descargar, no tiene sentido copiarlo en el backup
    private val carpetaTextos = File(context.noBackupFilesDir, "cuentos")

    override suspend fun getCuentos(idioma: Idioma): Result<List<Cuento>> = resultado {
        firestore.collection(COLECCION_CUENTOS).get().await()
            .documents
            .mapNotNull { it.aCuento(idioma) }
            .sortedBy { it.titulo.lowercase() }
    }

    override suspend fun getLineas(cuentoId: String, idioma: Idioma): Result<List<LineaCuento>> =
        resultado {
            val fichero = File(carpetaTextos, "${cuentoId}_${idioma.codigo}.txt")
            val contenido = try {
                val url = firestore.collection(COLECCION_CUENTOS).document(cuentoId).get().await()
                    .getString("txt_${idioma.codigo}")
                    .orEmpty()
                if (url.isEmpty()) throw NoSuchElementException("El cuento $cuentoId no tiene texto en ${idioma.codigo}")
                descargar(url).also { guardarEnDisco(fichero, it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin conexión: usamos la copia local si la hay
                leerDeDisco(fichero) ?: throw e
            }
            LineaCuento.desdeFichero(contenido)
        }

    override suspend fun getImagenes(cuentoId: String): Result<Map<String, String>> = resultado {
        val documento = firestore.collection(COLECCION_IMAGENES).document(cuentoId).get().await()
        documento.data.orEmpty()
            .filterKeys { it.startsWith(PREFIJO_IMAGEN) }
            .mapNotNull { (clave, valor) ->
                (valor as? String)?.takeIf { it.isNotEmpty() }?.let { clave.removePrefix(PREFIJO_IMAGEN) to it }
            }
            .toMap()
    }

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
