package com.carlossega.cuentameuncuento.data.precarga

import android.content.Context
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Descarga imágenes por adelantado para que se vean al momento cuando hagan falta.
 */
interface PrecargaImagenes {
    fun precargar(urls: Collection<String>)
}

/**
 * [PrecargaImagenes] con Coil: las imágenes se guardan en su caché de disco, que es la
 * misma que usan las pantallas. No se guardan en memoria para no ocupar RAM con
 * ilustraciones que quizá no se lleguen a ver.
 */
class CoilPrecargaImagenes @Inject constructor(
    @ApplicationContext private val context: Context
) : PrecargaImagenes {

    override fun precargar(urls: Collection<String>) {
        val cargador = SingletonImageLoader.get(context)
        urls.filter { it.isNotEmpty() }.distinct().forEach { url ->
            cargador.enqueue(
                ImageRequest.Builder(context)
                    .data(url)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    // Solo interesa la descarga; decodificarla a tamaño mínimo cuesta muy poco
                    .size(TAMANO_MINIMO)
                    .build()
            )
        }
    }

    private companion object {
        const val TAMANO_MINIMO = 64
    }
}
