package com.carlossega.cuentameuncuento.data.precarga

import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Prepara los cuentos mientras el niño está en el menú: las portadas y la primera
 * ilustración de cada uno. Las que vienen dentro de la app no hace falta descargarlas;
 * sirve para los cuentos nuevos que lleguen con un catálogo actualizado.
 */
class PrecargaCuentos @Inject constructor(
    private val cuentoRepository: CuentoRepository,
    private val precargaImagenes: PrecargaImagenes
) {

    suspend fun precargar(idioma: Idioma) {
        val cuentos = cuentoRepository.cuentos(idioma).catch { }.firstOrNull() ?: return
        precargaImagenes.precargar(cuentos.map { it.imagen })
        coroutineScope {
            cuentos.forEach { cuento ->
                launch {
                    val lineas = cuentoRepository.getLineas(cuento.id, idioma).getOrNull()
                    val ilustraciones = cuentoRepository.getImagenes(cuento.id).getOrNull() ?: return@launch
                    val primera = lineas?.firstNotNullOfOrNull { it.imagen }?.let(ilustraciones::get)
                    precargaImagenes.precargar(listOfNotNull(primera))
                }
            }
        }
    }
}
