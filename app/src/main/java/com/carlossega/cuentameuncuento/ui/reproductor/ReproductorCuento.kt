package com.carlossega.cuentameuncuento.ui.reproductor

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.load
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.audio.MusicaFondo
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.Modo
import com.carlossega.cuentameuncuento.databinding.ActivityReproductorCuentoBinding
import com.carlossega.cuentameuncuento.ui.common.BanderaAdapter
import com.carlossega.cuentameuncuento.ui.common.pantallaCompleta
import com.carlossega.cuentameuncuento.ui.common.vincularMusica
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReproductorCuento : AppCompatActivity() {

    @Inject
    lateinit var musicaFondo: MusicaFondo

    private val viewModel: ReproductorViewModel by viewModels()
    private lateinit var binding: ActivityReproductorCuentoBinding
    private val banderasTraduccion = BanderaAdapter(conOpcionNinguno = true)
    private var imagenMostrada: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReproductorCuentoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pantallaCompleta(binding.contenido)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        configurarModo(viewModel.modo)
        binding.imgBandera.setImageResource(viewModel.idioma.bandera)
        binding.btnMusica.vincularMusica(this, musicaFondo)

        binding.btnSalir.setOnClickListener { confirmarSalida() }
        binding.btnSiguiente.setOnClickListener { viewModel.siguiente() }
        binding.btnAnterior.setOnClickListener { viewModel.anterior() }
        binding.btnPlayPausa.setOnClickListener { viewModel.alternarNarracion() }
        binding.btnReintentar.setOnClickListener { viewModel.reintentar() }

        // Compatible con el gesto de "atrás predictivo" de Android 16+
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = confirmarSalida()
        })

        binding.spTraduccion.adapter = banderasTraduccion
        binding.spTraduccion.setSelection(
            banderasTraduccion.posicionDe(viewModel.uiState.value.idiomaTraduccion), false
        )
        binding.spTraduccion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.seleccionarTraduccion(banderasTraduccion.getItem(position))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::mostrar) }
                launch {
                    viewModel.mensajes.collect {
                        Toast.makeText(this@ReproductorCuento, it, Toast.LENGTH_LONG).show()
                    }
                }
                launch { viewModel.faltaVoz.collect { ofrecerInstalarVoz() } }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Al salir de la app la narración se para (Android 17 no deja sonar audio en segundo plano)
        viewModel.pausar()
    }

    private fun configurarModo(modo: Modo) {
        val leer = modo == Modo.LEER
        binding.btnAnterior.isVisible = leer
        binding.btnSiguiente.isVisible = leer
        binding.btnPlayPausa.isVisible = !leer
        binding.imgBandera.isVisible = !leer
    }

    private fun mostrar(estado: ReproductorUiState) {
        binding.progreso.isVisible = estado.cargando
        binding.grupoError.isVisible = estado.error
        binding.grupoCuento.isVisible = !estado.cargando && !estado.error

        binding.txtCuento.text = textoConPalabraResaltada(estado.lineaActual?.texto.orEmpty(), estado.palabra)
        binding.txtTraducido.text = estado.textoTraducido
        binding.txtTraducido.isVisible = estado.idiomaTraduccion != null

        binding.btnAnterior.isEnabled = estado.hayAnterior
        binding.btnAnterior.alpha = if (estado.hayAnterior) 1f else ALFA_DESACTIVADO
        binding.btnSiguiente.isEnabled = estado.haySiguiente
        binding.btnSiguiente.alpha = if (estado.haySiguiente) 1f else ALFA_DESACTIVADO

        binding.btnPlayPausa.setImageResource(if (estado.narrando) R.drawable.pause else R.drawable.play)
        binding.btnPlayPausa.contentDescription =
            getString(if (estado.narrando) R.string.pausar else R.string.escuchar)

        mostrarImagen(estado.imagenActual)
    }

    /** Mientras narra, la palabra que suena se colorea para que el niño siga la lectura */
    private fun textoConPalabraResaltada(texto: String, palabra: IntRange?): CharSequence {
        if (palabra == null || palabra.first < 0 || palabra.last >= texto.length) return texto
        return SpannableString(texto).apply {
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this@ReproductorCuento, R.color.palabra_narrada)),
                palabra.first,
                palabra.last + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun mostrarImagen(url: String?) {
        if (url == null || url == imagenMostrada) return
        imagenMostrada = url
        binding.imgFondo.load(url)
    }

    private fun ofrecerInstalarVoz() {
        AlertDialog.Builder(this)
            .setTitle(R.string.falta_voz)
            .setMessage(R.string.voz_idioma_no_disponible)
            .setPositiveButton(R.string.instalar) { _, _ -> abrirInstaladorVoz() }
            .setNegativeButton(R.string.ahora_no, null)
            .show()
    }

    private fun abrirInstaladorVoz() {
        try {
            startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.sin_instalador_voz, Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmarSalida() {
        AlertDialog.Builder(this)
            .setTitle(R.string.volver_seleccion)
            .setMessage(R.string.seguro)
            .setPositiveButton(R.string.si) { _, _ -> finish() }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    companion object {
        const val EXTRA_CUENTO = "cuento"
        const val EXTRA_IDIOMA = "idioma"
        const val EXTRA_MODO = "modo"
        private const val ALFA_DESACTIVADO = 0.35f

        fun intent(context: Context, cuentoId: String, idioma: Idioma, modo: Modo): Intent =
            Intent(context, ReproductorCuento::class.java)
                .putExtra(EXTRA_CUENTO, cuentoId)
                .putExtra(EXTRA_IDIOMA, idioma.codigo)
                .putExtra(EXTRA_MODO, modo.codigo)
    }
}
