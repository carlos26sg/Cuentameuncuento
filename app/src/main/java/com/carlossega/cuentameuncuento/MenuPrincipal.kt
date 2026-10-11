package com.carlossega.cuentameuncuento

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.carlossega.cuentameuncuento.ui.menu.MenuViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuPrincipal : AppCompatActivity() {

    // UI Components
    private lateinit var btnLeer: Button
    private lateinit var btnReproducir: Button
    private lateinit var btnSalir: Button
    private lateinit var btnActPerfil: Button
    private lateinit var btnFavorito: Button
    private lateinit var txtInfo: TextView

    // ViewModel con Hilt
    private val viewModel: MenuViewModel by viewModels()

    companion object {
        private const val TAG = "MenuPrincipal"

        // Variables estáticas públicas para compatibilidad temporal
        @JvmField
        var mp: MediaPlayer? = null

        @JvmField
        var btn_musica: Button? = null

        @JvmField
        var btn_sin_sonido: Button? = null

        @JvmField
        var usuario: Usuario = Usuario()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)

        PantallaCompleta().pantallaCompleta(window.decorView)

        initViews()
        initMusic()
        observeViewModel()
        setupListeners()
    }

    private fun initViews() {
        btnLeer = findViewById(R.id.btn_leer)
        btnReproducir = findViewById(R.id.btn_reproducir)
        btnSalir = findViewById(R.id.btn_salir)
        btnActPerfil = findViewById(R.id.btn_act_perfil)
        btn_musica = findViewById(R.id.btn_musica)
        btn_sin_sonido = findViewById(R.id.btn_sin_musica)
        txtInfo = findViewById(R.id.txt_menu_info)
        btnFavorito = findViewById(R.id.btn_favorito)

        btnActPerfil.text = "⚙️ Configuración"
        btnSalir.text = getString(R.string.salir)
        btnLeer.text = getString(R.string.leer)
        btnReproducir.text = getString(R.string.reproducir)
        btnFavorito.text = getString(R.string.favorito)
    }

    private fun initMusic() {
        mp = MediaPlayer.create(this, R.raw.hilo_musical).apply {
            isLooping = true
            start()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.usuario.collect { user ->
                usuario = user
                updateUI(user)
            }
        }
    }

    private fun updateUI(user: Usuario) {
        txtInfo.text = if (user.nombre.isNotEmpty()) {
            getString(R.string.bienvenido) + " " + user.nombre
        } else {
            getString(R.string.bienvenido)
        }
    }

    private fun setupListeners() {
        btn_musica?.setOnClickListener {
            mp?.pause()
            btn_musica?.visibility = View.GONE
            btn_sin_sonido?.visibility = View.VISIBLE
        }

        btn_sin_sonido?.setOnClickListener {
            mp?.start()
            btn_musica?.visibility = View.VISIBLE
            btn_sin_sonido?.visibility = View.GONE
        }

        btnSalir.setOnClickListener {
            finishAffinity()
            System.exit(0)
        }

        btnLeer.setOnClickListener {
            startActivity(Intent(this, SeleccionCuento::class.java).apply {
                putExtra("modo", "leer")
                putExtra("idioma", viewModel.getIdioma())
            })
        }

        btnReproducir.setOnClickListener {
            startActivity(Intent(this, SeleccionCuento::class.java).apply {
                putExtra("modo", "reproducir")
                putExtra("idioma", viewModel.getIdioma())
            })
        }

        btnActPerfil.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
        }

        btnFavorito.setOnClickListener { abrirFavorito() }
    }

    private fun abrirFavorito() {
        when {
            usuario.nombre.isEmpty() ->
                Toast.makeText(this, getString(R.string.inicia_sesion_acceso), Toast.LENGTH_LONG).show()
            usuario.modo_fav.isEmpty() || usuario.favorito.isEmpty() ->
                Toast.makeText(this, getString(R.string.config_perfil), Toast.LENGTH_LONG).show()
            else -> startActivity(Intent(this, ReproductorCuento::class.java).apply {
                putExtra("modo", usuario.modo_fav)
                putExtra("idioma", viewModel.getIdioma())
                putExtra("cuento", usuario.favorito)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserData()
        mp?.let {
            if (it.isPlaying) {
                btn_musica?.visibility = View.VISIBLE
                btn_sin_sonido?.visibility = View.GONE
            } else {
                btn_musica?.visibility = View.GONE
                btn_sin_sonido?.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mp?.release()
        mp = null
    }
}
