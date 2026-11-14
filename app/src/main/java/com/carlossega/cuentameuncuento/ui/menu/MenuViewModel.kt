package com.carlossega.cuentameuncuento.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.data.model.Lectura
import com.carlossega.cuentameuncuento.data.precarga.PrecargaCuentos
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Cuento a medias, con su título para el botón "Seguir con «…»" */
data class SeguirLeyendo(val lectura: Lectura, val titulo: String)

/**
 * ViewModel del menú: cuento a medias y descarga por adelantado de los cuentos nuevos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MenuViewModel @Inject constructor(
    preferenciasRepository: PreferenciasRepository,
    cuentoRepository: CuentoRepository,
    precargaCuentos: PrecargaCuentos
) : ViewModel() {

    val seguirLeyendo: StateFlow<SeguirLeyendo?> = preferenciasRepository.lectura
        .flatMapLatest { lectura ->
            if (lectura == null) {
                flowOf(null)
            } else {
                cuentoRepository.cuentos(lectura.idioma)
                    .map { cuentos -> cuentos.firstOrNull { it.id == lectura.cuentoId }?.let { SeguirLeyendo(lectura, it.titulo) } }
                    .catch { emit(null) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            precargaCuentos.precargar(preferenciasRepository.preferencias.first().idioma)
        }
    }
}
