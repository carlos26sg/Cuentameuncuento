package com.carlossega.cuentameuncuento

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.carlossega.cuentameuncuento.ui.menu.MenuPrincipal

/**
 * Pantalla de inicio con la API SplashScreen: muestra el icono mientras arranca la app
 * y abre el menú. La app no tiene usuarios, así que no hay nada que esperar.
 * Esta clase no se puede mover de paquete: es la que abre el icono del launcher.
 * Lint avisa por el nombre, pero usa la API oficial SplashScreen y no dibuja nada propio.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MenuPrincipal::class.java))
        finish()
    }
}
