package com.carlossega.cuentameuncuento.data.catalogo

import android.util.Log
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LineaCuento
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import com.carlossega.cuentameuncuento.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.Collator
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Cuentos a partir del catálogo. Sustituye a Firestore: antes cada pantalla encadenaba
 * varias peticiones (lista, documento de imágenes, fichero de texto por idioma);
 * ahora todo sale de un único fichero que ya viene dentro de la app.
 *
 * - Al primer uso se lee el catálogo incluido o el último descargado, el más reciente.
 * - Una vez por sesión se descarga el publicado; si es más nuevo, se guarda y las
 *   pantallas abiertas se actualizan solas.
 * - Las imágenes que ya vienen en la app se cargan de ella; las nuevas, de internet.
 */
@Singleton
class CatalogoCuentoRepository @Inject constructor(
    private val fuentes: FuentesCatalogo,
    @ApplicationScope private val segundoPlano: CoroutineScope
) : CuentoRepository {

    private data class Estado(val catalogo: Catalogo, val incluidas: Set<String>)

    private val estado = MutableStateFlow<Estado?>(null)
    private val carga = Mutex()
    private val actualizacionLanzada = AtomicBoolean(false)

    override fun cuentos(idioma: Idioma): Flow<List<Cuento>> = flow {
        actual()
        emitAll(estado.filterNotNull().map { it.cuentos(idioma) }.distinctUntilChanged())
    }

    override suspend fun getLineas(cuentoId: String, idioma: Idioma): Result<List<LineaCuento>> = resultado {
        buscar(cuentoId).lineas.map { LineaCuento(it.texto[idioma.codigo].orEmpty(), it.imagen) }
    }

    override suspend fun getImagenes(cuentoId: String): Result<Map<String, String>> = resultado {
        val estado = actual()
        estado.buscar(cuentoId).ilustraciones.mapValues { (_, ruta) -> estado.url(ruta) }
    }

    private suspend fun buscar(cuentoId: String): CuentoCatalogo = actual().buscar(cuentoId)

    private fun Estado.buscar(cuentoId: String): CuentoCatalogo =
        catalogo.cuentos.firstOrNull { it.id == cuentoId }
            ?: throw NoSuchElementException("No existe el cuento $cuentoId")

    private fun Estado.url(ruta: String) = fuentes.url(ruta, incluida = ruta in incluidas)

    private fun Estado.cuentos(idioma: Idioma): List<Cuento> =
        catalogo.cuentos
            .mapNotNull { cuento ->
                val titulo = cuento.titulo[idioma.codigo]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                Cuento(
                    id = cuento.id,
                    titulo = titulo,
                    descripcion = cuento.descripcion[idioma.codigo].orEmpty(),
                    imagen = url(cuento.portada)
                )
            }
            // Orden alfabético del idioma: "Árbol" va antes que "Zorro"
            .sortedWith(compareBy(Collator.getInstance(Locale.forLanguageTag(idioma.etiquetaLocale))) { it.titulo })

    private suspend fun actual(): Estado {
        estado.value?.let { return it }
        return carga.withLock {
            estado.value ?: cargarLocal().also {
                estado.value = it
                actualizarEnSegundoPlano()
            }
        }
    }

    private suspend fun cargarLocal(): Estado {
        val incluido = checkNotNull(Catalogo.leer(fuentes.incluido())) { "El catálogo incluido en la app no es válido" }
        val guardado = try {
            fuentes.guardado()?.let(Catalogo::leer)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo leer el catálogo guardado", e)
            null
        }
        // Tras actualizar la app, el incluido puede ser más nuevo que el descargado
        val catalogo = if (guardado != null && guardado.generado > incluido.generado) guardado else incluido
        return Estado(catalogo, incluido.rutasImagenes)
    }

    private fun actualizarEnSegundoPlano() {
        if (!actualizacionLanzada.compareAndSet(false, true)) return
        segundoPlano.launch {
            try {
                val texto = fuentes.descargar()
                val nuevo = Catalogo.leer(texto)
                if (nuevo == null) {
                    Log.w(TAG, "El catálogo publicado no es válido para esta versión de la app")
                    return@launch
                }
                val actual = estado.value ?: return@launch
                if (nuevo.generado > actual.catalogo.generado) {
                    fuentes.guardar(texto)
                    estado.value = actual.copy(catalogo = nuevo)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin conexión: se sigue con el catálogo que ya hay
                Log.w(TAG, "No se pudo actualizar el catálogo", e)
            }
        }
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
    }
}
