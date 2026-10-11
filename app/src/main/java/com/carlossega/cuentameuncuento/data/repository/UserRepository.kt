package com.carlossega.cuentameuncuento.data.repository

import com.carlossega.cuentameuncuento.Usuario
import com.carlossega.cuentameuncuento.auth.AuthManager
import com.carlossega.cuentameuncuento.auth.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gestionar datos del usuario
 * Centraliza toda la lógica de usuario
 */
@Singleton
class UserRepository @Inject constructor(
    private val authManager: AuthManager
) {

    /**
     * Obtiene el usuario actual desde las preferencias locales
     */
    fun getCurrentUser(): Usuario {
        val prefs = authManager.getLocalPreferences()
        return Usuario(
            nombre = prefs.nombre,
            idioma = prefs.idioma,
            favorito = prefs.favorito,
            modo_fav = prefs.modoFav,
            mail = authManager.getCurrentUserId() ?: ""
        )
    }

    /**
     * Guarda las preferencias del usuario
     */
    suspend fun saveUserPreferences(
        nombre: String,
        idioma: String,
        favorito: String = "",
        modoFav: String = ""
    ): Result<Unit> {
        return authManager.saveUserPreferences(nombre, idioma, favorito, modoFav)
    }

    /**
     * Carga las preferencias desde Firestore
     */
    suspend fun loadUserPreferences(): Result<UserPreferences> {
        return authManager.loadUserPreferences()
    }

    /**
     * Verifica si hay un usuario autenticado
     */
    fun isUserSignedIn(): Boolean {
        return authManager.isUserSignedIn()
    }

    /**
     * Cierra sesión
     */
    fun signOut() {
        authManager.signOut()
    }

    /**
     * Inicia sesión anónima
     */
    suspend fun signInAnonymously(): Result<String> {
        return authManager.signInAnonymously()
    }
}
