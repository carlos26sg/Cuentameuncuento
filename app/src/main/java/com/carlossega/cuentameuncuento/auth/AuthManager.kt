package com.carlossega.cuentameuncuento.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Gestor de autenticación simplificado usando Firebase Anonymous Auth
 * Elimina el sistema inseguro de contraseñas cifradas
 */
class AuthManager(context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // SharedPreferences cifradas para datos locales
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_user_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Inicia sesión de forma anónima (sin contraseña)
     * Firebase gestiona la persistencia automáticamente
     */
    suspend fun signInAnonymously(): Result<String> {
        return try {
            val result = auth.signInAnonymously().await()
            val userId = result.user?.uid ?: return Result.failure(Exception("No user ID"))
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el usuario actual si existe
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Verifica si hay un usuario autenticado
     */
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Guarda las preferencias del usuario (idioma, nombre, favorito)
     * Se guardan localmente cifradas Y en Firestore para sincronización
     */
    suspend fun saveUserPreferences(
        nombre: String,
        idioma: String,
        favorito: String = "",
        modoFav: String = ""
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("No user signed in"))

            // Guardar localmente (cifrado)
            encryptedPrefs.edit().apply {
                putString("nombre", nombre)
                putString("idioma", idioma)
                putString("favorito", favorito)
                putString("modo_fav", modoFav)
                putString("user_id", userId)
                apply()
            }

            // Guardar en Firestore (sin contraseñas)
            val userDoc = hashMapOf(
                "nombre" to nombre,
                "idioma" to idioma,
                "favorito" to favorito,
                "modo_fav" to modoFav,
                "last_updated" to System.currentTimeMillis()
            )

            db.collection("usuarios")
                .document(userId)
                .set(userDoc)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Carga las preferencias del usuario desde Firestore
     */
    suspend fun loadUserPreferences(): Result<UserPreferences> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("No user signed in"))

            val document = db.collection("usuarios")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val prefs = UserPreferences(
                    nombre = document.getString("nombre") ?: "",
                    idioma = document.getString("idioma") ?: "esp",
                    favorito = document.getString("favorito") ?: "",
                    modoFav = document.getString("modo_fav") ?: ""
                )

                // Guardar también localmente
                encryptedPrefs.edit().apply {
                    putString("nombre", prefs.nombre)
                    putString("idioma", prefs.idioma)
                    putString("favorito", prefs.favorito)
                    putString("modo_fav", prefs.modoFav)
                    apply()
                }

                Result.success(prefs)
            } else {
                // Primera vez, usar preferencias por defecto
                Result.success(UserPreferences())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene las preferencias locales (más rápido que Firestore)
     */
    fun getLocalPreferences(): UserPreferences {
        return UserPreferences(
            nombre = encryptedPrefs.getString("nombre", "") ?: "",
            idioma = encryptedPrefs.getString("idioma", "esp") ?: "esp",
            favorito = encryptedPrefs.getString("favorito", "") ?: "",
            modoFav = encryptedPrefs.getString("modo_fav", "") ?: ""
        )
    }

    /**
     * Cierra sesión (opcional, normalmente no es necesario)
     */
    fun signOut() {
        auth.signOut()
        encryptedPrefs.edit().clear().apply()
    }
}

/**
 * Modelo de preferencias de usuario
 */
data class UserPreferences(
    val nombre: String = "",
    val idioma: String = "esp",
    val favorito: String = "",
    val modoFav: String = ""
)
