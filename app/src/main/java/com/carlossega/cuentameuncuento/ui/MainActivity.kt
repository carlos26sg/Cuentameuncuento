package com.carlossega.cuentameuncuento.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.activity.viewModels
import com.carlossega.cuentameuncuento.audio.MusicaFondo
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import com.carlossega.cuentameuncuento.ui.comun.LocalMusica
import com.carlossega.cuentameuncuento.ui.tema.CuentameTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Letra y tamaño elegidos para los cuentos: el tema los necesita en todas las pantallas */
@HiltViewModel
class MainViewModel @Inject constructor(preferencias: PreferenciasRepository) : ViewModel() {
    val preferencias: StateFlow<Preferencias> = preferencias.preferencias
        .stateIn(viewModelScope, SharingStarted.Eagerly, Preferencias())
}

/**
 * Única Activity de la app: todas las pantallas son de Compose ([Navegacion]).
 * Pantalla completa: las barras del sistema se ocultan y aparecen deslizando desde el borde.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var musicaFondo: MusicaFondo

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        setContent {
            val preferencias by viewModel.preferencias.collectAsStateWithLifecycle()
            CuentameTheme(preferencias.letra, preferencias.tamanoTexto) {
                CompositionLocalProvider(LocalMusica provides musicaFondo) {
                    Navegacion()
                }
            }
        }
    }
}
