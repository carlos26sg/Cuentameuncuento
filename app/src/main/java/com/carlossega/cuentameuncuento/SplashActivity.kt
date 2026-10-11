package com.carlossega.cuentameuncuento

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.carlossega.cuentameuncuento.data.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Nueva pantalla de inicio simplificada
 * - Autentica automáticamente de forma anónima
 * - Carga preferencias del usuario
 * - Navega directamente al menú principal
 */
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Pantalla completa
        PantallaCompleta().pantallaCompleta(window.decorView)

        // Inicializar autenticación
        inicializarApp()
    }

    private fun inicializarApp() {
        lifecycleScope.launch {
            try {
                // Si no hay usuario autenticado, crear uno anónimo
                if (!userRepository.isUserSignedIn()) {
                    userRepository.signInAnonymously()
                }

                // Pequeña pausa para mostrar splash
                delay(1500)

                // Cargar preferencias si existen
                userRepository.loadUserPreferences()

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
