package com.carlossega.cuentameuncuento.ui.biblioteca

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.Lectura
import com.carlossega.cuentameuncuento.data.precarga.PrecargaCuentos
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Un cuento de la biblioteca con su marca de favorito */
data class CuentoEnLista(val cuento: Cuento, val favorito: Boolean)

/** Cuento a medias, para la tarjeta "Seguir leyendo" */
data class SeguirLeyendo(val lectura: Lectura, val cuento: Cuento)

sealed interface BibliotecaUiState {
    data object Cargando : BibliotecaUiState
    data class Lista(val idioma: Idioma, val cuentos: List<CuentoEnLista>) : BibliotecaUiState
    data class Vacia(val idioma: Idioma) : BibliotecaUiState
    data class Error(val idioma: Idioma) : BibliotecaUiState
}

/**
 * Biblioteca, la pantalla de inicio: todos los cuentos del idioma elegido (favoritos
 * primero) y el cuento a medias. Mientras se muestra, descarga por adelantado las
 * imágenes de los cuentos nuevos que lleguen con el catálogo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BibliotecaViewModel @Inject constructor(
    private val cuentoRepository: CuentoRepository,
    private val preferenciasRepository: PreferenciasRepository,
    precargaCuentos: PrecargaCuentos
) : ViewModel() {

    private val reintentos = MutableStateFlow(0)

    val uiState: StateFlow<BibliotecaUiState> = combine(
        preferenciasRepository.preferencias.map { it.idioma }.distinctUntilChanged(),
        reintentos
    ) { idioma, _ -> idioma }
        .flatMapLatest { idioma ->
            // Los favoritos van primero según cómo estaban al abrir la biblioteca: si se
            // reordenara al tocar un corazón, la portada saltaría y desaparecería de la vista
            var primeros: Set<String>? = null
            combine(
                cuentoRepository.cuentos(idioma),
                preferenciasRepository.preferencias.map { it.favoritos }.distinctUntilChanged()
            ) { cuentos, favoritos ->
                if (cuentos.isEmpty()) {
                    BibliotecaUiState.Vacia(idioma)
                } else {
                    val orden = primeros ?: favoritos.also { primeros = it }
                    BibliotecaUiState.Lista(
                        idioma,
                        cuentos.map { CuentoEnLista(it, it.id in favoritos) }.sortedByDescending { it.cuento.id in orden }
                    )
                }
            }
                .onStart<BibliotecaUiState> { emit(BibliotecaUiState.Cargando) }
                .catch { emit(BibliotecaUiState.Error(idioma)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BibliotecaUiState.Cargando)

    val seguirLeyendo: StateFlow<SeguirLeyendo?> = preferenciasRepository.lectura
        .flatMapLatest { lectura ->
            if (lectura == null) {
                flowOf(null)
            } else {
                cuentoRepository.cuentos(lectura.idioma)
                    .map { cuentos -> cuentos.firstOrNull { it.id == lectura.cuentoId }?.let { SeguirLeyendo(lectura, it) } }
                    .catch { emit(null) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            precargaCuentos.precargar(preferenciasRepository.preferencias.first().idioma)
        }
    }

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
