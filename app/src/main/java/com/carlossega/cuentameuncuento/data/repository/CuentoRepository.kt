package com.carlossega.cuentameuncuento.data.repository

import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LineaCuento
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a los cuentos. La implementación real es [FirestoreCuentoRepository];
 * los tests usan una versión falsa.
 *
 * Todo lo ya descargado se devuelve al momento desde el dispositivo y se actualiza
 * después en segundo plano, para no hacer esperar al niño cada vez que abre la app.
 */
interface CuentoRepository {

    /**
     * Cuentos que tienen título en [idioma], ordenados por título.
     * Emite primero los guardados (si los hay) y después los del servidor si cambian.
     * Solo falla si no hay ni copia guardada ni conexión.
     */
    fun cuentos(idioma: Idioma): Flow<List<Cuento>>

    /** Texto del cuento en [idioma], ya separado en líneas */
    suspend fun getLineas(cuentoId: String, idioma: Idioma): Result<List<LineaCuento>>

    /** URLs de las ilustraciones del cuento, indexadas por número de imagen */
    suspend fun getImagenes(cuentoId: String): Result<Map<String, String>>
}
