package com.carlossega.cuentameuncuento.ui.ficha

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.data.model.Cuento
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

data class FichaUiState(
    val cuento: Cuento? = null,
    /** Primera ilustración: fondo desenfocado de la ficha */
    val fondo: String? = null,
    /** Unas pocas ilustraciones repartidas por el cuento, para "Un vistazo" */
    val vistazo: List<String> = emptyList(),
    val error: Boolean = false
)

/**
 * Ficha de un cuento: portada, datos, descripción y un vistazo a sus ilustraciones,
 * además de si es favorito y si se dejó a medias.
 */
@HiltViewModel
class FichaViewModel @Inject constructor(
    private val cuentoRepository: CuentoRepository,
    private val preferenciasRepository: PreferenciasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val cuentoId: String = savedStateHandle.get<String>(CLAVE_CUENTO).orEmpty()
    val idioma: Idioma = Idioma.desdeCodigo(savedStateHandle.get<String>(CLAVE_IDIOMA))

    private val _uiState = MutableStateFlow(FichaUiState())
    val uiState: StateFlow<FichaUiState> = _uiState.asStateFlow()

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
                _uiState.value = FichaUiState(error = true)
                return@launch
            }
            _uiState.value = FichaUiState(cuento = cuento, fondo = cuento.imagen)

            // Las ilustraciones en el orden en que aparecen en el cuento
            val orden = cuentoRepository.getLineas(cuentoId, idioma).getOrNull().orEmpty()
                .mapNotNull { it.imagen }
                .distinct()
            val urls = cuentoRepository.getImagenes(cuentoId).getOrNull().orEmpty()
            val ilustraciones = orden.mapNotNull(urls::get)
            _uiState.value = _uiState.value.copy(
                fondo = ilustraciones.firstOrNull() ?: cuento.imagen,
                vistazo = repartidas(ilustraciones.drop(1).ifEmpty { ilustraciones }, VISTAZO)
            )
        }
    }

    fun alternarFavorito() {
        viewModelScope.launch { preferenciasRepository.alternarFavorito(cuentoId) }
    }

    private companion object {
        const val CLAVE_CUENTO = "cuento"
        const val CLAVE_IDIOMA = "idioma"
        const val VISTAZO = 3

        /** [cuantas] elementos repartidos a lo largo de la lista, sin destripar el final */
        fun <T> repartidas(lista: List<T>, cuantas: Int): List<T> {
            if (lista.size <= cuantas) return lista
            val paso = lista.size.toFloat() / cuantas
            return List(cuantas) { lista[(it * paso).toInt()] }
        }
    }
}
