package com.carlossega.cuentameuncuento.ui.seleccion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles para la carga de cuentos
 */
sealed interface CuentosUiState {
    data object Cargando : CuentosUiState
    data class Listo(val cuentos: List<Cuento>) : CuentosUiState
    data object Vacio : CuentosUiState
    data object Error : CuentosUiState
}

/**
 * ViewModel para SeleccionCuento. Los cuentos se cargan una sola vez por idioma
 * (antes se volvían a añadir a la lista cada vez que se volvía a la pantalla).
 */
@HiltViewModel
class SeleccionViewModel @Inject constructor(
    private val cuentoRepository: CuentoRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _idioma = MutableStateFlow(
        Idioma.desdeCodigo(savedStateHandle.get<String>(SeleccionCuento.EXTRA_IDIOMA))
    )
    val idioma: StateFlow<Idioma> = _idioma.asStateFlow()

    private val _uiState = MutableStateFlow<CuentosUiState>(CuentosUiState.Cargando)
    val uiState: StateFlow<CuentosUiState> = _uiState.asStateFlow()

    private var carga: Job? = null

    init {
        cargar()
    }

    fun cambiarIdioma(idioma: Idioma) {
        if (idioma == _idioma.value) return
        _idioma.value = idioma
        savedStateHandle[SeleccionCuento.EXTRA_IDIOMA] = idioma.codigo
        cargar()
    }

    fun reintentar() = cargar()

    private fun cargar() {
        carga?.cancel()
        _uiState.value = CuentosUiState.Cargando
        carga = viewModelScope.launch {
            _uiState.value = cuentoRepository.getCuentos(_idioma.value).fold(
                onSuccess = { if (it.isEmpty()) CuentosUiState.Vacio else CuentosUiState.Listo(it) },
                onFailure = { CuentosUiState.Error }
            )
        }
    }
}
