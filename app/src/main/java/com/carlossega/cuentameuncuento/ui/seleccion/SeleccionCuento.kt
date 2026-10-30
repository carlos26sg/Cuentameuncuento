package com.carlossega.cuentameuncuento.ui.seleccion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.Modo
import com.carlossega.cuentameuncuento.databinding.ActivitySeleccionCuentoBinding
import com.carlossega.cuentameuncuento.ui.common.BanderaAdapter
import com.carlossega.cuentameuncuento.ui.common.pantallaCompleta
import com.carlossega.cuentameuncuento.ui.reproductor.ReproductorCuento
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SeleccionCuento : AppCompatActivity() {

    private val viewModel: SeleccionViewModel by viewModels()
    private lateinit var binding: ActivitySeleccionCuentoBinding
    private val banderas = BanderaAdapter()

    private val modo: Modo by lazy {
        Modo.desdeCodigo(intent.getStringExtra(EXTRA_MODO)) ?: Modo.LEER
    }

    private val adaptador = AdaptadorCuentos { cuento ->
        startActivity(ReproductorCuento.intent(this, cuento.id, viewModel.idioma.value, modo))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionCuentoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pantallaCompleta(binding.contenido)

        binding.btnAtras.setOnClickListener { finish() }
        binding.btnReintentar.setOnClickListener { viewModel.reintentar() }

        // En tablets o en vertical caben varias columnas
        val anchoDp = resources.configuration.screenWidthDp
        binding.rvCuentos.layoutManager = GridLayoutManager(this, (anchoDp / ANCHO_COLUMNA_DP).coerceIn(1, 3))
        binding.rvCuentos.adapter = adaptador

        binding.spIdioma.adapter = banderas
        binding.spIdioma.setSelection(banderas.posicionDe(viewModel.idioma.value), false)
        binding.spIdioma.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                banderas.getItem(position)?.let(viewModel::cambiarIdioma)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::mostrar)
            }
        }
    }

    private fun mostrar(estado: CuentosUiState) {
        binding.progreso.isVisible = estado is CuentosUiState.Cargando
        binding.rvCuentos.isVisible = estado is CuentosUiState.Listo
        binding.grupoMensaje.isVisible = estado is CuentosUiState.Vacio || estado is CuentosUiState.Error
        binding.btnReintentar.isVisible = estado is CuentosUiState.Error
        when (estado) {
            is CuentosUiState.Listo -> adaptador.submitList(estado.cuentos)
            CuentosUiState.Vacio -> binding.txtMensaje.setText(R.string.sin_cuentos)
            CuentosUiState.Error -> binding.txtMensaje.setText(R.string.error_cargar_cuentos)
            CuentosUiState.Cargando -> Unit
        }
    }

    companion object {
        const val EXTRA_MODO = "modo"
        const val EXTRA_IDIOMA = "idioma"
        private const val ANCHO_COLUMNA_DP = 420

        fun intent(context: Context, modo: Modo, idioma: Idioma): Intent =
            Intent(context, SeleccionCuento::class.java)
                .putExtra(EXTRA_MODO, modo.codigo)
                .putExtra(EXTRA_IDIOMA, idioma.codigo)
    }
}
