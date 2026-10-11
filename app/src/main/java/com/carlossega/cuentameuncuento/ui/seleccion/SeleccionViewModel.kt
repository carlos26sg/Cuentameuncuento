package com.carlossega.cuentameuncuento.ui.seleccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.Cuento
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles para la carga de cuentos
 */
sealed class CuentosUiState {
    object Loading : CuentosUiState()
    data class Success(val cuentos: List<Cuento>) : CuentosUiState()
    data class Error(val message: String) : CuentosUiState()
}

/**
 * ViewModel para SeleccionCuento
 * Gestiona la carga y filtrado de cuentos
 */
@HiltViewModel
class SeleccionViewModel @Inject constructor(
    private val cuentoRepository: CuentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CuentosUiState>(CuentosUiState.Loading)
    val uiState: StateFlow<CuentosUiState> = _uiState.asStateFlow()

    /**
     * Carga los cuentos desde Firestore
     */
    fun loadCuentos(idioma: String) {
        viewModelScope.launch {
            _uiState.value = CuentosUiState.Loading

            val result = cuentoRepository.getCuentos(idioma)

            _uiState.value = result.fold(
                onSuccess = { cuentos ->
                    if (cuentos.isEmpty()) {
                        CuentosUiState.Error("No se encontraron cuentos")
                    } else {
                        CuentosUiState.Success(cuentos)
                    }
                },
                onFailure = { exception ->
                    CuentosUiState.Error(exception.message ?: "Error desconocido")
                }
            )
        }
    }
}
