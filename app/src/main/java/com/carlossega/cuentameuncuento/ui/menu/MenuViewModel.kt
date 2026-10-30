package com.carlossega.cuentameuncuento.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel para MenuPrincipal. Las preferencias se sincronizan con Firestore en el
 * splash y al guardar en Ajustes; aquí solo se observan las locales.
 */
@HiltViewModel
class MenuViewModel @Inject constructor(
    userRepository: UserRepository
) : ViewModel() {

    val preferencias: StateFlow<Preferencias> = userRepository.preferencias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Preferencias())
}
