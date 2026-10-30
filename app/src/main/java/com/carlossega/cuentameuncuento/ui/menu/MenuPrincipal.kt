package com.carlossega.cuentameuncuento.ui.menu

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.audio.MusicaFondo
import com.carlossega.cuentameuncuento.data.model.Modo
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.databinding.ActivityMenuPrincipalBinding
import com.carlossega.cuentameuncuento.ui.ajustes.AjustesActivity
import com.carlossega.cuentameuncuento.ui.common.pantallaCompleta
import com.carlossega.cuentameuncuento.ui.common.vincularMusica
import com.carlossega.cuentameuncuento.ui.reproductor.ReproductorCuento
import com.carlossega.cuentameuncuento.ui.seleccion.SeleccionCuento
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MenuPrincipal : AppCompatActivity() {

    @Inject
    lateinit var musicaFondo: MusicaFondo

    private val viewModel: MenuViewModel by viewModels()
    private lateinit var binding: ActivityMenuPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pantallaCompleta(binding.contenido)

        binding.btnMusica.vincularMusica(this, musicaFondo)
        binding.btnLeer.setOnClickListener { abrirSeleccion(Modo.LEER) }
        binding.btnEscuchar.setOnClickListener { abrirSeleccion(Modo.ESCUCHAR) }
        binding.btnFavorito.setOnClickListener { abrirFavorito(viewModel.preferencias.value) }
        binding.btnAjustes.setOnClickListener { startActivity(AjustesActivity.intent(this)) }
        binding.btnSalir.setOnClickListener { finishAffinity() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.preferencias.collect(::mostrarBienvenida)
            }
        }
    }

    private fun mostrarBienvenida(preferencias: Preferencias) {
        binding.txtBienvenida.text = if (preferencias.nombre.isNotEmpty()) {
            getString(R.string.bienvenido_nombre, preferencias.nombre)
        } else {
            getString(R.string.bienvenido)
        }
    }

    private fun abrirSeleccion(modo: Modo) {
        startActivity(SeleccionCuento.intent(this, modo, viewModel.preferencias.value.idioma))
    }

    private fun abrirFavorito(preferencias: Preferencias) {
        val modo = preferencias.modoFavorito
        if (!preferencias.tieneFavorito || modo == null) {
            Toast.makeText(this, R.string.config_favorito, Toast.LENGTH_LONG).show()
            return
        }
        startActivity(ReproductorCuento.intent(this, preferencias.favorito, preferencias.idioma, modo))
    }
}
