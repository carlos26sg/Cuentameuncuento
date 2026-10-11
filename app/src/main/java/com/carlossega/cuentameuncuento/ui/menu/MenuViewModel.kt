package com.carlossega.cuentameuncuento.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossega.cuentameuncuento.Usuario
import com.carlossega.cuentameuncuento.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para MenuPrincipal
 * Gestiona el estado y la lógica de negocio separada de la UI
 */
@HiltViewModel
class MenuViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _usuario = MutableStateFlow(Usuario())
    val usuario: StateFlow<Usuario> = _usuario.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserData()
    }

    /**
     * Carga los datos del usuario
     */
    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cargar desde local primero
                _usuario.value = userRepository.getCurrentUser()

                // Intentar sincronizar desde Firestore
                val result = userRepository.loadUserPreferences()
                result.onSuccess { prefs ->
                    _usuario.value = Usuario(
                        nombre = prefs.nombre,
                        idioma = prefs.idioma,
                        favorito = prefs.favorito,
                        modo_fav = prefs.modoFav,
                        mail = userRepository.getCurrentUser().mail
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene el idioma actual del usuario
     */
    fun getIdioma(): String {
        return _usuario.value.idioma.ifEmpty { "esp" }
    }
}
