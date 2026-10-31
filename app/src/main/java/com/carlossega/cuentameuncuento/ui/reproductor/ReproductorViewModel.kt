package com.carlossega.cuentameuncuento.ui.reproductor

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.audio.FalloNarrador
import com.carlossega.cuentameuncuento.audio.Locucion
import com.carlossega.cuentameuncuento.audio.Narrador
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LineaCuento
import com.carlossega.cuentameuncuento.data.model.Modo
import com.carlossega.cuentameuncuento.data.model.VelocidadVoz
import com.carlossega.cuentameuncuento.data.precarga.PrecargaImagenes
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReproductorUiState(
    val cargando: Boolean = true,
    val error: Boolean = false,
    val lineas: List<LineaCuento> = emptyList(),
    val indice: Int = 0,
    val idiomaTraduccion: Idioma? = null,
    val traduccion: List<LineaCuento> = emptyList(),
    val imagenes: Map<String, String> = emptyMap(),
    val narrando: Boolean = false,
    /** Posición en el texto de la palabra que está sonando, para resaltarla */
    val palabra: IntRange? = null
) {
    val lineaActual: LineaCuento? get() = lineas.getOrNull(indice)

    val textoTraducido: String get() = traduccion.getOrNull(indice)?.texto.orEmpty()

    /** La ilustración se mantiene hasta que una línea posterior indica otra */
    val imagenActual: String?
        get() = (indice downTo 0).asSequence()
            .mapNotNull { lineas.getOrNull(it)?.imagen }
            .firstOrNull()
            ?.let(imagenes::get)

    val hayAnterior: Boolean get() = indice > 0

    val haySiguiente: Boolean
        get() = indice < lineas.lastIndex && lineaActual?.esFinal != true
}

/**
 * Estado del reproductor: carga del cuento, línea actual, traducción y narración por voz.
 * Sobrevive a los giros de pantalla y guarda la línea actual si el sistema mata el proceso.
 */
@HiltViewModel
class ReproductorViewModel @Inject constructor(
    private val cuentoRepository: CuentoRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val precargaImagenes: PrecargaImagenes,
    private val narrador: Narrador,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val cuentoId: String = savedStateHandle.get<String>(ReproductorCuento.EXTRA_CUENTO).orEmpty()
    val idioma: Idioma = Idioma.desdeCodigo(savedStateHandle.get<String>(ReproductorCuento.EXTRA_IDIOMA))
    val modo: Modo = Modo.desdeCodigo(savedStateHandle.get<String>(ReproductorCuento.EXTRA_MODO)) ?: Modo.LEER

    private val _uiState = MutableStateFlow(
        ReproductorUiState(indice = savedStateHandle.get<Int>(CLAVE_INDICE) ?: 0)
    )
    val uiState: StateFlow<ReproductorUiState> = _uiState.asStateFlow()

    private val _mensajes = Channel<Int>(Channel.BUFFERED)
    val mensajes: Flow<Int> = _mensajes.receiveAsFlow()

    /** Emite cuando falta la voz del idioma, para ofrecer instalarla */
    private val _faltaVoz = Channel<Idioma>(Channel.CONFLATED)
    val faltaVoz: Flow<Idioma> = _faltaVoz.receiveAsFlow()

    private var cargaTraduccion: Job? = null
    private var velocidad = VelocidadVoz.NORMAL

    init {
        cargar()
    }

    fun reintentar() = cargar()

    fun siguiente() {
        if (_uiState.value.haySiguiente) irA(_uiState.value.indice + 1)
    }

    fun anterior() {
        if (_uiState.value.hayAnterior) irA(_uiState.value.indice - 1)
    }

    fun seleccionarTraduccion(idiomaTraduccion: Idioma?) {
        if (idiomaTraduccion == _uiState.value.idiomaTraduccion) return
        cargaTraduccion?.cancel()
        _uiState.update { it.copy(idiomaTraduccion = idiomaTraduccion, traduccion = emptyList()) }
        if (idiomaTraduccion == null) return
        cargaTraduccion = viewModelScope.launch {
            cuentoRepository.getLineas(cuentoId, idiomaTraduccion)
                .onSuccess { lineas -> _uiState.update { it.copy(traduccion = lineas) } }
                .onFailure { mostrarMensaje(R.string.error_traduccion) }
        }
    }

    fun alternarNarracion() {
        if (_uiState.value.narrando) pausar() else narrar()
    }

    fun pausar() {
        if (!_uiState.value.narrando) return
        _uiState.update { it.copy(narrando = false, palabra = null) }
        narrador.detener()
    }

    private fun narrar() {
        if (_uiState.value.lineas.isEmpty()) return
        _uiState.update { it.copy(narrando = true) }
        narrarLineaActual(pausaPreviaMs = 0)
    }

    private fun narrarLineaActual(pausaPreviaMs: Long) {
        val linea = _uiState.value.lineaActual ?: return pausar()
        narrador.hablar(
            locucion = Locucion(linea.texto, idioma, velocidad, pausaPreviaMs),
            alPalabra = { inicio, fin ->
                if (_uiState.value.narrando) _uiState.update { it.copy(palabra = inicio until fin) }
            },
            alTerminar = {
                if (_uiState.value.narrando) {
                    if (_uiState.value.haySiguiente) {
                        irA(_uiState.value.indice + 1)
                        // Pausa más larga cuando cambia la ilustración, como al pasar de página
                        val cambiaEscena = _uiState.value.lineaActual?.imagen != null
                        narrarLineaActual(if (cambiaEscena) PAUSA_ESCENA_MS else PAUSA_FRASE_MS)
                    } else {
                        // Fin del cuento: se vuelve al principio, como en la versión anterior
                        _uiState.update { it.copy(narrando = false) }
                        irA(0)
                    }
                }
            },
            alFallar = { fallo ->
                _uiState.update { it.copy(narrando = false, palabra = null) }
                when (fallo) {
                    FalloNarrador.IDIOMA_NO_DISPONIBLE -> _faltaVoz.trySend(idioma)
                    FalloNarrador.MOTOR_NO_DISPONIBLE -> mostrarMensaje(R.string.voz_no_disponible)
                    FalloNarrador.ERROR_REPRODUCCION -> mostrarMensaje(R.string.voz_error)
                }
            }
        )
    }

    private fun cargar() {
        _uiState.update { it.copy(cargando = true, error = false) }
        viewModelScope.launch {
            val imagenes = async { cuentoRepository.getImagenes(cuentoId).getOrDefault(emptyMap()) }
            val lineas = cuentoRepository.getLineas(cuentoId, idioma).getOrNull().orEmpty()
            velocidad = preferenciasRepository.preferencias.first().velocidadVoz
            _uiState.update {
                it.copy(
                    cargando = false,
                    error = lineas.isEmpty(),
                    lineas = lineas,
                    indice = it.indice.coerceIn(0, (lineas.size - 1).coerceAtLeast(0)),
                    imagenes = imagenes.await()
                )
            }
            // Todas las ilustraciones del cuento, para que pasar de página sea inmediato
            precargaImagenes.precargar(_uiState.value.imagenes.values)
        }
    }

    private fun irA(indice: Int) {
        savedStateHandle[CLAVE_INDICE] = indice
        _uiState.update { it.copy(indice = indice, palabra = null) }
    }

    private fun mostrarMensaje(@StringRes mensaje: Int) {
        _mensajes.trySend(mensaje)
    }

    override fun onCleared() {
        narrador.liberar()
    }

    private companion object {
        const val CLAVE_INDICE = "indice"
        const val PAUSA_FRASE_MS = 400L
        const val PAUSA_ESCENA_MS = 900L
    }
}
