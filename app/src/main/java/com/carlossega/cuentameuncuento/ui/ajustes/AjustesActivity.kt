package com.carlossega.cuentameuncuento.ui.ajustes

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.carlossega.cuentameuncuento.BuildConfig
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Modo
import com.carlossega.cuentameuncuento.databinding.ActivityAjustesBinding
import com.carlossega.cuentameuncuento.ui.common.BanderaAdapter
import com.carlossega.cuentameuncuento.ui.common.pantallaCompleta
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AjustesActivity : AppCompatActivity() {

    private val viewModel: AjustesViewModel by viewModels()
    private lateinit var binding: ActivityAjustesBinding

    private val banderas = BanderaAdapter()
    private val modos: List<Modo?> = listOf(null, Modo.LEER, Modo.ESCUCHAR)
    private var cuentosMostrados: List<Cuento>? = null
    private var valoresIniciales = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAjustesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pantallaCompleta(binding.contenido)

        binding.txtVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        binding.btnAtras.setOnClickListener { finish() }
        binding.btnGuardar.setOnClickListener { viewModel.guardar() }
        binding.btnPrivacidad.setOnClickListener { abrirPoliticaPrivacidad() }

        binding.spIdioma.adapter = banderas
        binding.spModo.adapter = adaptadorTextos(
            listOf(getString(R.string.sin_seleccion), getString(R.string.leer), getString(R.string.escuchar))
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::mostrar) }
                launch {
                    viewModel.mensajes.collect {
                        Toast.makeText(this@AjustesActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun mostrar(estado: AjustesUiState) {
        if (!valoresIniciales && !estado.cargando) {
            valoresIniciales = true
            binding.spIdioma.setSelection(banderas.posicionDe(estado.idioma), false)
            binding.spModo.setSelection(modos.indexOf(estado.modoFavorito).coerceAtLeast(0), false)
            binding.spIdioma.alSeleccionar { banderas.getItem(it)?.let(viewModel::cambiarIdioma) }
            binding.spModo.alSeleccionar { viewModel.cambiarModo(modos[it]) }
        }

        if (!estado.cargando && estado.cuentos !== cuentosMostrados) {
            cuentosMostrados = estado.cuentos
            binding.spFavorito.onItemSelectedListener = null
            binding.spFavorito.adapter = adaptadorTextos(
                listOf(getString(R.string.sin_seleccion)) + estado.cuentos.map { it.titulo }
            )
            val posicion = estado.cuentos.indexOfFirst { it.id == estado.favorito } + 1
            binding.spFavorito.setSelection(posicion, false)
            binding.spFavorito.alSeleccionar { pos ->
                viewModel.cambiarFavorito(estado.cuentos.getOrNull(pos - 1)?.id.orEmpty())
            }
        }
    }

    private fun abrirPoliticaPrivacidad() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.url_privacidad).toUri()))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.sin_navegador, Toast.LENGTH_SHORT).show()
        }
    }

    private fun adaptadorTextos(textos: List<String>) =
        ArrayAdapter(this, R.layout.spinner_item_text, textos).apply {
            setDropDownViewResource(R.layout.spinner_item_dropdown)
        }

    /**
     * Spinner también avisa cuando la selección cambia por código; solo se reenvían
     * los cambios a una posición distinta de la última conocida.
     */
    private fun Spinner.alSeleccionar(accion: (Int) -> Unit) {
        var ultima = selectedItemPosition
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == ultima) return
                ultima = position
                accion(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    companion object {
        fun intent(context: Context) = Intent(context, AjustesActivity::class.java)
    }
}
