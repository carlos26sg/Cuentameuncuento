package com.carlossega.cuentameuncuento.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.prefs.PreferenciasStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Música de fondo de toda la app.
 *
 * - Suena solo con la app en primer plano (se registra en ProcessLifecycleOwner).
 * - Pide el foco de audio: se para si otra app empieza a sonar y baja el volumen
 *   mientras habla el narrador u otra app pide "ducking".
 * - Recuerda si el usuario la ha silenciado.
 */
@Singleton
class MusicaFondo @Inject constructor(
    @ApplicationContext private val context: Context,
    private val store: PreferenciasStore
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val audioManager = context.getSystemService(AudioManager::class.java)

    private val _activa = MutableStateFlow(true)
    val activa: StateFlow<Boolean> = _activa.asStateFlow()

    private var reproductor: MediaPlayer? = null
    private var enPrimerPlano = false
    private var pausadaPorOtraApp = false

    private val atributos = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val peticionFoco = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(atributos)
        .setOnAudioFocusChangeListener(::alCambiarFoco)
        .build()

    init {
        scope.launch {
            _activa.value = store.musicaActiva.first()
            actualizar()
        }
    }

    fun alternar() {
        val nueva = !_activa.value
        _activa.value = nueva
        pausadaPorOtraApp = false
        scope.launch { store.guardarMusicaActiva(nueva) }
        actualizar()
    }

    override fun onStart(owner: LifecycleOwner) {
        enPrimerPlano = true
        pausadaPorOtraApp = false
        actualizar()
    }

    override fun onStop(owner: LifecycleOwner) {
        enPrimerPlano = false
        actualizar()
    }

    private fun actualizar() {
        if (_activa.value && enPrimerPlano && !pausadaPorOtraApp) reproducir() else pausar()
    }

    private fun reproducir() {
        val mp = reproductor ?: MediaPlayer.create(
            context, R.raw.hilo_musical, atributos, audioManager.generateAudioSessionId()
        )?.apply { isLooping = true }?.also { reproductor = it } ?: return
        if (mp.isPlaying) return
        if (audioManager.requestAudioFocus(peticionFoco) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mp.setVolume(VOLUMEN_NORMAL, VOLUMEN_NORMAL)
            mp.start()
        }
    }

    private fun pausar() {
        reproductor?.takeIf { it.isPlaying }?.pause()
        audioManager.abandonAudioFocusRequest(peticionFoco)
    }

    private fun alCambiarFoco(cambio: Int) {
        val mp = reproductor ?: return
        when (cambio) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                mp.setVolume(VOLUMEN_NORMAL, VOLUMEN_NORMAL)
                if (_activa.value && enPrimerPlano && !mp.isPlaying) mp.start()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mp.setVolume(VOLUMEN_BAJO, VOLUMEN_BAJO)
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (mp.isPlaying) mp.pause()
            AudioManager.AUDIOFOCUS_LOSS -> {
                pausadaPorOtraApp = true
                pausar()
            }
        }
    }

    private companion object {
        const val VOLUMEN_NORMAL = 1f
        const val VOLUMEN_BAJO = 0.2f
    }
}
