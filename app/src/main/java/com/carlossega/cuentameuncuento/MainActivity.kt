package com.carlossega.cuentameuncuento

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.carlossega.cuentameuncuento.auth.AuthManager

/**
 * MainActivity simplificada - Ahora solo es informativa
 * El login automático se hace en SplashActivity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var txtInfo: TextView
    private lateinit var txtPerfil: TextView
    private lateinit var btnComenzar: Button
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Pantalla completa
        val pantallaCompleta = PantallaCompleta()
        pantallaCompleta.pantallaCompleta(window.decorView)

        authManager = AuthManager(this)

        // Inicializar vistas
        txtInfo = findViewById(R.id.txt_info)
        txtPerfil = findViewById(R.id.txt_perfil)
        btnComenzar = findViewById(R.id.img_btn_comenzar)

        txtInfo.text = getString(R.string.info_developer) + "2.0"
        btnComenzar.text = getString(R.string.comenzar)

        // Cargar preferencias locales
        val prefs = authManager.getLocalPreferences()

        // Mostrar mensaje de bienvenida
        txtPerfil.text = if (prefs.nombre.isNotEmpty()) {
            getString(R.string.bienvenido) + ", ${prefs.nombre}"
        } else {
            getString(R.string.bienvenido)
        }

        btnComenzar.setOnClickListener {
            comenzar()
        }
    }

    private fun comenzar() {
        val intent = Intent(this, MenuPrincipal::class.java)
        startActivity(intent)
        finish()
    }
}
