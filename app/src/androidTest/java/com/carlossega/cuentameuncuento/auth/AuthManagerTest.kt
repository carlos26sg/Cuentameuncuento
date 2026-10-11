package com.carlossega.cuentameuncuento.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests para AuthManager
 * Verifica que la autenticación funcione correctamente
 */
@RunWith(AndroidJUnit4::class)
class AuthManagerTest {

    private lateinit var authManager: AuthManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // Inicializar Firebase si no está ya
        try {
            FirebaseApp.initializeApp(context)
        } catch (e: Exception) {
            // Ya está inicializado
        }

        authManager = AuthManager(context)
    }

    @Test
    fun authManager_isCreated() {
        assertThat(authManager).isNotNull()
    }

    @Test
    fun getLocalPreferences_returnsDefaultValues() {
        val prefs = authManager.getLocalPreferences()

        assertThat(prefs).isNotNull()
        assertThat(prefs.idioma).isEqualTo("esp") // Valor por defecto
    }

    @Test
    fun saveUserPreferences_andLoad_returnsCorrectData() = runTest {
        // Given
        val testNombre = "Test User"
        val testIdioma = "eng"

        // When
        val saveResult = authManager.saveUserPreferences(
            nombre = testNombre,
            idioma = testIdioma
        )

        // Then
        assertThat(saveResult.isSuccess).isTrue()

        val localPrefs = authManager.getLocalPreferences()
        assertThat(localPrefs.nombre).isEqualTo(testNombre)
        assertThat(localPrefs.idioma).isEqualTo(testIdioma)
    }
}
