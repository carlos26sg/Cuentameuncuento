package com.carlossega.cuentameuncuento

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.carlossega.cuentameuncuento.auth.AuthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Nueva pantalla de inicio simplificada
 * - Autentica automáticamente de forma anónima
 * - Carga preferencias del usuario
 * - Navega directamente al menú principal
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Pantalla completa
        val pantallaCompleta = PantallaCompleta()
        pantallaCompleta.pantallaCompleta(window.decorView)

        authManager = AuthManager(this)

        // Inicializar autenticación
        inicializarApp()
    }

    private fun inicializarApp() {
        lifecycleScope.launch {
            try {
                // Si no hay usuario autenticado, crear uno anónimo
                if (!authManager.isUserSignedIn()) {
                    authManager.signInAnonymously()
                }

                // Pequeña pausa para mostrar splash
                delay(1500)

                // Cargar preferencias si existen
                authManager.loadUserPreferences()

                // Ir al menú principal
                navegarAMenuPrincipal()

            } catch (e: Exception) {
                // Si falla, continuar de todos modos con preferencias por defecto
                delay(1500)
                navegarAMenuPrincipal()
            }
        }
    }

    private fun navegarAMenuPrincipal() {
        val intent = Intent(this, MenuPrincipal::class.java)
        startActivity(intent)
        finish()
    }
}
