package com.carlossega.cuentameuncuento.data.repository

import com.carlossega.cuentameuncuento.data.model.Preferencias
import kotlinx.coroutines.flow.Flow

/**
 * Preferencias de la app. Solo se guardan en el dispositivo: la app no tiene
 * usuarios ni envía datos de los niños a ningún servidor.
 * La implementación real es [com.carlossega.cuentameuncuento.data.prefs.PreferenciasStore].
 */
interface PreferenciasRepository {

    /** Emite de nuevo cada vez que cambian */
    val preferencias: Flow<Preferencias>

    suspend fun guardar(preferencias: Preferencias)
}
