package com.carlossega.cuentameuncuento.data.repository

import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LetraCuento
import com.carlossega.cuentameuncuento.data.model.Lectura
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.model.VelocidadVoz
import kotlinx.coroutines.flow.Flow

/**
 * Preferencias de la app. Solo se guardan en el dispositivo: la app no tiene
 * usuarios ni envía datos de los niños a ningún servidor.
 * La implementación real es [com.carlossega.cuentameuncuento.data.prefs.PreferenciasStore].
 * Cada cambio se guarda al momento: en Ajustes no hay botón "Guardar".
 */
interface PreferenciasRepository {

    /** Emite de nuevo cada vez que cambian */
    val preferencias: Flow<Preferencias>

    /** Cuento a medias, o null si no hay ninguno */
    val lectura: Flow<Lectura?>

    suspend fun cambiarIdioma(idioma: Idioma)

    suspend fun cambiarVelocidad(velocidad: VelocidadVoz)

    suspend fun cambiarLetra(letra: LetraCuento)

    suspend fun alternarFavorito(cuentoId: String)

    /** null al terminar el cuento */
    suspend fun guardarLectura(lectura: Lectura?)
}
