package com.carlossega.cuentameuncuento.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.precarga.PrecargaCuentos
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para MenuPrincipal: idioma y favorito para los botones del menú.
 * Mientras se muestra el menú, descarga por adelantado los cuentos del idioma elegido.
 */
@HiltViewModel
class MenuViewModel @Inject constructor(
    preferenciasRepository: PreferenciasRepository,
    precargaCuentos: PrecargaCuentos
) : ViewModel() {

    val preferencias: StateFlow<Preferencias> = preferenciasRepository.preferencias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Preferencias())

    init {
        viewModelScope.launch {
            precargaCuentos.precargar(preferenciasRepository.preferencias.first().idioma)
        }
    }
}
