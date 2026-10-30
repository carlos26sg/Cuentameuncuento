package com.carlossega.cuentameuncuento.data.repository

import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LineaCuento

/**
 * Acceso a los cuentos. La implementación real es [FirestoreCuentoRepository];
 * los tests usan una versión falsa.
 */
interface CuentoRepository {

    /** Cuentos que tienen título en [idioma], ordenados por título */
    suspend fun getCuentos(idioma: Idioma): Result<List<Cuento>>

    /** Texto del cuento en [idioma], ya separado en líneas */
    suspend fun getLineas(cuentoId: String, idioma: Idioma): Result<List<LineaCuento>>

    /** URLs de las ilustraciones del cuento, indexadas por número de imagen */
    suspend fun getImagenes(cuentoId: String): Result<Map<String, String>>
}
