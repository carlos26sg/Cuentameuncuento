package com.carlossega.cuentameuncuento.ui.portada

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortadaUiState(
    val titulo: String = "",
    val descripcion: String = "",
    /** Primera ilustración del cuento (fondo de la portada); la miniatura mientras tanto */
    val imagen: String? = null,
    val error: Boolean = false
)

/**
 * Portada de un cuento: título, descripción y primera ilustración, además de si es
 * favorito y si se dejó a medias.
 */
@HiltViewModel
class PortadaViewModel @Inject constructor(
    private val cuentoRepository: CuentoRepository,
    private val preferenciasRepository: PreferenciasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val cuentoId: String = savedStateHandle.get<String>(CLAVE_CUENTO).orEmpty()
    val idioma: Idioma = Idioma.desdeCodigo(savedStateHandle.get<String>(CLAVE_IDIOMA))

    private val _uiState = MutableStateFlow(PortadaUiState())
    val uiState: StateFlow<PortadaUiState> = _uiState.asStateFlow()

    val favorito: StateFlow<Boolean> = preferenciasRepository.preferencias
        .map { cuentoId in it.favoritos }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Línea por la que se quedó, si este cuento está a medias en este idioma */
    val lineaGuardada: StateFlow<Int?> = preferenciasRepository.lectura
        .map { l -> l?.takeIf { it.cuentoId == cuentoId && it.idioma == idioma && it.linea > 0 }?.linea }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            val cuento = runCatching { cuentoRepository.cuentos(idioma).first() }
                .getOrNull()
                ?.firstOrNull { it.id == cuentoId }
            if (cuento == null) {
                _uiState.value = PortadaUiState(error = true)
                return@launch
            }
            _uiState.value = PortadaUiState(cuento.titulo, cuento.descripcion, cuento.imagen)

            val primera = cuentoRepository.getLineas(cuentoId, idioma).getOrNull()?.firstNotNullOfOrNull { it.imagen }
            val ilustracion = primera?.let { cuentoRepository.getImagenes(cuentoId).getOrNull()?.get(it) }
            if (ilustracion != null) _uiState.value = _uiState.value.copy(imagen = ilustracion)
        }
    }

    fun alternarFavorito() {
        viewModelScope.launch { preferenciasRepository.alternarFavorito(cuentoId) }
    }

    private companion object {
        const val CLAVE_CUENTO = "cuento"
        const val CLAVE_IDIOMA = "idioma"
    }
}
