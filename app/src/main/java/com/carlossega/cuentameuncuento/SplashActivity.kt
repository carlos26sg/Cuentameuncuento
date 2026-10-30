package com.carlossega.cuentameuncuento

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.carlossega.cuentameuncuento.data.repository.UserRepository
import com.carlossega.cuentameuncuento.ui.menu.MenuPrincipal
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * Pantalla de inicio con la API SplashScreen.
 * Inicia la sesión anónima y sincroniza las preferencias antes de abrir el menú.
 * Esta clase no se puede mover de paquete: es la que abre el icono del launcher.
 * Lint avisa por el nombre, pero usa la API oficial SplashScreen y no dibuja nada propio.
 */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    private var preparada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { !preparada }

        lifecycleScope.launch {
            // Sin conexión no esperamos más de lo necesario: se usan las preferencias locales
            withTimeoutOrNull(TIEMPO_MAXIMO_MS) {
                if (userRepository.asegurarSesion()) userRepository.sincronizar()
            }
            preparada = true
            startActivity(Intent(this@SplashActivity, MenuPrincipal::class.java))
            finish()
        }
    }

    private companion object {
        const val TIEMPO_MAXIMO_MS = 4_000L
    }
}
