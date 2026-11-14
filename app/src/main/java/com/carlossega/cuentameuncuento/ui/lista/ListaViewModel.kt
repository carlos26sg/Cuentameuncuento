package com.carlossega.cuentameuncuento.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Un cuento de la lista con su marca de favorito */
data class CuentoEnLista(val cuento: Cuento, val favorito: Boolean)

sealed interface ListaUiState {
    data object Cargando : ListaUiState
    data class Lista(val idioma: Idioma, val cuentos: List<CuentoEnLista>) : ListaUiState
    data class Vacia(val idioma: Idioma) : ListaUiState
    data class Error(val idioma: Idioma) : ListaUiState
}

/**
 * Lista de cuentos del idioma elegido, con los favoritos primero.
 * El idioma es el de Ajustes: cambiarlo aquí también lo cambia allí.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ListaViewModel @Inject constructor(
    private val cuentoRepository: CuentoRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    private val reintentos = MutableStateFlow(0)

    val uiState: StateFlow<ListaUiState> = combine(
        preferenciasRepository.preferencias.map { it.idioma }.distinctUntilChanged(),
        reintentos
    ) { idioma, _ -> idioma }
        .flatMapLatest { idioma ->
            // Los favoritos van primero según cómo estaban al abrir la lista: si se reordenara
            // al tocar un corazón, la tarjeta saltaría arriba y desaparecería de la vista
            var primeros: Set<String>? = null
            combine(
                cuentoRepository.cuentos(idioma),
                preferenciasRepository.preferencias.map { it.favoritos }.distinctUntilChanged()
            ) { cuentos, favoritos ->
                if (cuentos.isEmpty()) {
                    ListaUiState.Vacia(idioma)
                } else {
                    val orden = primeros ?: favoritos.also { primeros = it }
                    ListaUiState.Lista(
                        idioma,
                        cuentos.map { CuentoEnLista(it, it.id in favoritos) }.sortedByDescending { it.cuento.id in orden }
                    )
                }
            }
                .onStart<ListaUiState> { emit(ListaUiState.Cargando) }
                .catch { emit(ListaUiState.Error(idioma)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListaUiState.Cargando)

    fun cambiarIdioma(idioma: Idioma) {
        viewModelScope.launch { preferenciasRepository.cambiarIdioma(idioma) }
    }

    fun alternarFavorito(cuentoId: String) {
        viewModelScope.launch { preferenciasRepository.alternarFavorito(cuentoId) }
    }

    fun reintentar() {
        reintentos.value++
    }
}
