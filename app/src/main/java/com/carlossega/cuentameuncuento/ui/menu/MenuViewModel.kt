package com.carlossega.cuentameuncuento.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel para MenuPrincipal: idioma y favorito para los botones del menú
 */
@HiltViewModel
class MenuViewModel @Inject constructor(
    preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    val preferencias: StateFlow<Preferencias> = preferenciasRepository.preferencias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Preferencias())
}
