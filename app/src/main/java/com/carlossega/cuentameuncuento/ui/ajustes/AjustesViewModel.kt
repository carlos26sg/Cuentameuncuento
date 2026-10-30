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
import com.carlossega.cuentameuncuento.data.repository.UserRepository
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

data class AjustesUiState(
    val cargando: Boolean = true,
    val ocupado: Boolean = false,
    val idioma: Idioma = Idioma.ESPANOL,
    val modoFavorito: Modo? = null,
    val favorito: String = "",
    val cuentos: List<Cuento> = emptyList()
)

sealed interface AjustesEvento {
    data class Mensaje(@StringRes val texto: Int) : AjustesEvento
    data object DatosBorrados : AjustesEvento
}

/**
 * ViewModel de la pantalla de Ajustes (antes "Perfil"). El favorito se guarda por id
 * del cuento; antes se guardaba el título y el botón Favorito no encontraba el cuento.
 */
@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cuentoRepository: CuentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AjustesUiState())
    val uiState: StateFlow<AjustesUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<AjustesEvento>(Channel.BUFFERED)
    val eventos: Flow<AjustesEvento> = _eventos.receiveAsFlow()

    private var preferencias = Preferencias()
    private var cargaCuentos: Job? = null

    init {
        viewModelScope.launch {
            preferencias = userRepository.preferencias.first()
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
        val nuevas = preferencias.copy(
            idioma = estado.idioma,
            modoFavorito = estado.modoFavorito,
            favorito = estado.favorito
        )
        _uiState.update { it.copy(ocupado = true) }
        viewModelScope.launch {
            val resultado = userRepository.guardar(nuevas)
            if (resultado.isSuccess) preferencias = nuevas
            _uiState.update { it.copy(ocupado = false) }
            _eventos.send(
                AjustesEvento.Mensaje(if (resultado.isSuccess) R.string.ajustes_guardados else R.string.error_guardar)
            )
        }
    }

    fun borrarDatos() {
        _uiState.update { it.copy(ocupado = true) }
        viewModelScope.launch {
            val resultado = userRepository.borrarDatos()
            _uiState.update { it.copy(ocupado = false) }
            _eventos.send(
                if (resultado.isSuccess) AjustesEvento.DatosBorrados
                else AjustesEvento.Mensaje(R.string.error_borrar_datos)
            )
        }
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
