package com.carlossega.cuentameuncuento.ui.ajustes

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.Modo
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
import kotlin.coroutines.cancellation.CancellationException

data class AjustesUiState(
    val cargando: Boolean = true,
    val idioma: Idioma = Idioma.ESPANOL,
    val modoFavorito: Modo? = null,
    val favorito: String = "",
    val cuentos: List<Cuento> = emptyList()
)

/**
 * ViewModel de la pantalla de Ajustes. El favorito se guarda por id del cuento;
 * antes se guardaba el título y el botón Favorito no encontraba el cuento.
 */
@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val cuentoRepository: CuentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AjustesUiState())
    val uiState: StateFlow<AjustesUiState> = _uiState.asStateFlow()

    private val _mensajes = Channel<Int>(Channel.BUFFERED)
    val mensajes: Flow<Int> = _mensajes.receiveAsFlow()

    private var cargaCuentos: Job? = null

    init {
        viewModelScope.launch {
            val preferencias = preferenciasRepository.preferencias.first()
            _uiState.update {
                it.copy(
                    idioma = preferencias.idioma,
                    modoFavorito = preferencias.modoFavorito,
                    favorito = preferencias.favorito
                )
            }
            cargarCuentos(preferencias.idioma)
        }
    }

    /** Los títulos del selector de favorito se muestran en el idioma elegido */
    fun cambiarIdioma(idioma: Idioma) {
        if (idioma == _uiState.value.idioma) return
        _uiState.update { it.copy(idioma = idioma) }
        cargarCuentos(idioma)
    }

    fun cambiarModo(modo: Modo?) = _uiState.update { it.copy(modoFavorito = modo) }

    fun cambiarFavorito(cuentoId: String) = _uiState.update { it.copy(favorito = cuentoId) }

    fun guardar() {
        val estado = _uiState.value
        viewModelScope.launch {
            val mensaje = try {
                preferenciasRepository.guardar(
                    Preferencias(
                        idioma = estado.idioma,
                        favorito = estado.favorito,
                        modoFavorito = estado.modoFavorito
                    )
                )
                R.string.ajustes_guardados
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                R.string.error_guardar
            }
            mostrarMensaje(mensaje)
        }
    }

    private fun mostrarMensaje(@StringRes mensaje: Int) {
        _mensajes.trySend(mensaje)
    }

    private fun cargarCuentos(idioma: Idioma) {
        cargaCuentos?.cancel()
        _uiState.update { it.copy(cargando = true) }
        cargaCuentos = viewModelScope.launch {
            val cuentos = cuentoRepository.getCuentos(idioma).getOrDefault(emptyList())
            _uiState.update { it.copy(cargando = false, cuentos = cuentos) }
        }
    }
}
