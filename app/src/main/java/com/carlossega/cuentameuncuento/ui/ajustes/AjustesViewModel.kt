package com.carlossega.cuentameuncuento.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LetraCuento
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.model.TamanoTexto
import com.carlossega.cuentameuncuento.data.model.VelocidadVoz
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ajustes: cada cambio se guarda al momento (ya no hay botón "Guardar").
 * Los favoritos se marcan con el corazón de cada cuento, no aquí.
 */
@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    val preferencias: StateFlow<Preferencias?> = preferenciasRepository.preferencias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun cambiarIdioma(idioma: Idioma) {
        viewModelScope.launch { preferenciasRepository.cambiarIdioma(idioma) }
    }

    fun cambiarVelocidad(velocidad: VelocidadVoz) {
        viewModelScope.launch { preferenciasRepository.cambiarVelocidad(velocidad) }
    }

    fun cambiarLetra(letra: LetraCuento) {
        viewModelScope.launch { preferenciasRepository.cambiarLetra(letra) }
    }

    fun cambiarTamanoTexto(tamano: TamanoTexto) {
        viewModelScope.launch { preferenciasRepository.cambiarTamanoTexto(tamano) }
    }
}
