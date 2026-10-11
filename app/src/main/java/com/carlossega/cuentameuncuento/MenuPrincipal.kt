package com.carlossega.cuentameuncuento

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.carlossega.cuentameuncuento.auth.AuthManager
import kotlinx.coroutines.launch

class MenuPrincipal : AppCompatActivity() {

    // UI Components
    private lateinit var btnLeer: Button
    private lateinit var btnReproducir: Button
    private lateinit var btnSalir: Button
    private lateinit var btnActPerfil: Button
    private lateinit var btnFavorito: Button
    private lateinit var txtInfo: TextView

    // Auth y datos
    private lateinit var authManager: AuthManager

    companion object {
        private const val TAG = "MenuPrincipal"

        // Variables estáticas públicas para compatibilidad con código Java antiguo
        // TODO: Eliminar cuando todo se migre a Kotlin y se use arquitectura MVVM
        // @JvmField hace que sean accesibles como campos públicos estáticos desde Java
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

        // Pantalla completa
        PantallaCompleta().pantallaCompleta(window.decorView)

        authManager = AuthManager(this)

        // Inicializar vistas
        initViews()

        // Inicializar música de fondo
        initMusic()

        // Cargar datos del usuario
        loadUserData()

        // Configurar listeners
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

        // Establecer textos
        btnActPerfil.text = getString(R.string.perfil)
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

    private fun loadUserData() {
        // Cargar preferencias locales primero (rápido)
        val prefs = authManager.getLocalPreferences()
        usuario.nombre = prefs.nombre
        usuario.idioma = prefs.idioma
        usuario.favorito = prefs.favorito
        usuario.modo_fav = prefs.modoFav

        updateUI()

        // Luego sincronizar desde Firestore (más lento)
        lifecycleScope.launch {
            try {
                val result = authManager.loadUserPreferences()
                result.onSuccess { userPrefs ->
                    usuario.nombre = userPrefs.nombre
                    usuario.idioma = userPrefs.idioma
                    usuario.favorito = userPrefs.favorito
                    usuario.modo_fav = userPrefs.modoFav
                    updateUI()
                    Log.d(TAG, "Datos del usuario cargados desde Firestore")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando datos del usuario: ${e.message}")
            }
        }
    }

    private fun updateUI() {
        // Actualizar mensaje de bienvenida
        txtInfo.text = if (usuario.nombre.isNotEmpty()) {
            getString(R.string.bienvenido) + " " + usuario.nombre
        } else {
            getString(R.string.bienvenido)
        }

        // Detectar idioma si no está establecido
        if (usuario.idioma.isEmpty()) {
            usuario.idioma = detectarIdioma()
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
            val idioma = getIdioma()
            if (idioma.isEmpty()) return@setOnClickListener

            val intent = Intent(this, SeleccionCuento::class.java).apply {
                putExtra("modo", "leer")
                putExtra("idioma", idioma)
            }
            startActivity(intent)
            Log.d(TAG, "Iniciando SeleccionCuento en modo: leer, idioma: $idioma")
        }

        btnReproducir.setOnClickListener {
            val idioma = getIdioma()
            if (idioma.isEmpty()) return@setOnClickListener

            val intent = Intent(this, SeleccionCuento::class.java).apply {
                putExtra("modo", "reproducir")
                putExtra("idioma", idioma)
            }
            startActivity(intent)
            Log.d(TAG, "Iniciando SeleccionCuento en modo: reproducir, idioma: $idioma")
        }

        btnActPerfil.setOnClickListener {
            val idioma = getIdioma()
            if (idioma.isEmpty()) return@setOnClickListener

            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        btnFavorito.setOnClickListener {
            abrirFavorito()
        }
    }

    private fun abrirFavorito() {
        try {
            when {
                usuario.nombre.isEmpty() -> {
                    Toast.makeText(
                        this,
                        getString(R.string.inicia_sesion_acceso),
                        Toast.LENGTH_LONG
                    ).show()
                }
                usuario.modo_fav.isEmpty() || usuario.favorito.isEmpty() -> {
                    Toast.makeText(
                        this,
                        getString(R.string.config_perfil),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    val idioma = getIdioma()
                    val intent = Intent(this, ReproductorCuento::class.java).apply {
                        putExtra("modo", usuario.modo_fav)
                        putExtra("idioma", idioma)
                        putExtra("cuento", usuario.favorito)
                    }
                    startActivity(intent)
                    Log.d(TAG, "Iniciando favorito: ${usuario.favorito}, modo: ${usuario.modo_fav}")
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.espera), Toast.LENGTH_LONG).show()
            Log.e(TAG, "Error abriendo favorito: ${e.message}")
        }
    }

    private fun getIdioma(): String {
        return if (usuario.idioma.isNotEmpty()) {
            usuario.idioma
        } else {
            detectarIdioma()
        }
    }

    private fun detectarIdioma(): String {
        return when {
            btnLeer.text == "Leer" -> "esp"
            btnLeer.text == "Llegir" -> "cat"
            btnLeer.text == "Read" -> "eng"
            else -> "esp" // Default
        }
    }

    override fun onResume() {
        super.onResume()

        // Recargar preferencias por si se actualizaron en Perfil
        loadUserData()

        // Control del icono de música
        mp?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                btn_musica?.visibility = View.VISIBLE
                btn_sin_sonido?.visibility = View.GONE
            } else {
                btn_musica?.visibility = View.GONE
                btn_sin_sonido?.visibility = View.VISIBLE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // No pausar la música al cambiar de activity
    }

    override fun onDestroy() {
        super.onDestroy()
        mp?.release()
        mp = null
    }
}
