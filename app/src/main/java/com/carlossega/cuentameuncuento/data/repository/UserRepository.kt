package com.carlossega.cuentameuncuento.data.repository

import com.carlossega.cuentameuncuento.data.model.Preferencias
import kotlinx.coroutines.flow.Flow

/**
 * Usuario anónimo y sus preferencias. La implementación real es [FirebaseUserRepository].
 */
interface UserRepository {

    /** Preferencias locales; emite de nuevo cada vez que cambian */
    val preferencias: Flow<Preferencias>

    /** Inicia sesión anónima si todavía no hay usuario. Devuelve false si no se pudo */
    suspend fun asegurarSesion(): Boolean

    /** Descarga de Firestore las preferencias guardadas y actualiza las locales */
    suspend fun sincronizar(): Result<Unit>

    /** Guarda en local y en Firestore */
    suspend fun guardar(preferencias: Preferencias): Result<Unit>

    /** Borra los datos de Firestore, el usuario anónimo y las preferencias locales */
    suspend fun borrarDatos(): Result<Unit>
}
