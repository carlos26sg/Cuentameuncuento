package com.carlossega.cuentameuncuento

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.carlossega.cuentameuncuento.data.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity simplificada - Ahora solo es informativa
 * El login automático se hace en SplashActivity
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var txtInfo: TextView
    private lateinit var txtPerfil: TextView
    private lateinit var btnComenzar: Button

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Pantalla completa
        PantallaCompleta().pantallaCompleta(window.decorView)

        // Inicializar vistas
        txtInfo = findViewById(R.id.txt_info)
        txtPerfil = findViewById(R.id.txt_perfil)
        btnComenzar = findViewById(R.id.img_btn_comenzar)

        txtInfo.text = getString(R.string.info_developer) + "2.0"
        btnComenzar.text = getString(R.string.comenzar)

        // Cargar preferencias locales
        val usuario = userRepository.getCurrentUser()

        // Mostrar mensaje de bienvenida
        txtPerfil.text = if (usuario.nombre.isNotEmpty()) {
            getString(R.string.bienvenido) + ", ${usuario.nombre}"
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
