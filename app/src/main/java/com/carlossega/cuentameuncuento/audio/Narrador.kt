package com.carlossega.cuentameuncuento.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.carlossega.cuentameuncuento.data.model.Idioma
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

enum class FalloNarrador { MOTOR_NO_DISPONIBLE, IDIOMA_NO_DISPONIBLE, ERROR_REPRODUCCION }

/**
 * Lee textos en voz alta. Los callbacks se llaman siempre en el hilo principal.
 */
interface Narrador {
    fun hablar(texto: String, idioma: Idioma, alTerminar: () -> Unit, alFallar: (FalloNarrador) -> Unit)
    fun detener()
    fun liberar()
}

/**
 * [Narrador] con el TextToSpeech del sistema. Usa un único motor durante toda la vida
 * del objeto (antes se creaba y destruía uno por cada línea del cuento).
 */
class TtsNarrador @Inject constructor(
    @ApplicationContext context: Context
) : Narrador {

    private class Peticion(
        val id: String,
        val texto: String,
        val idioma: Idioma,
        val alTerminar: () -> Unit,
        val alFallar: (FalloNarrador) -> Unit
    )

    private enum class Estado { INICIANDO, LISTO, ERROR, LIBERADO }

    private val principal = Handler(Looper.getMainLooper())
    private var estado = Estado.INICIANDO
    private var pendiente: Peticion? = null
    private var actual: Peticion? = null
    private var idiomaConfigurado: Idioma? = null
    private var contador = 0

    private val tts = TextToSpeech(context.applicationContext) { status ->
        principal.post { alIniciar(status) }
    }

    init {
        tts.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                principal.post { terminar(utteranceId)?.alTerminar?.invoke() }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                principal.post { terminar(utteranceId)?.alFallar?.invoke(FalloNarrador.ERROR_REPRODUCCION) }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                principal.post { terminar(utteranceId)?.alFallar?.invoke(FalloNarrador.ERROR_REPRODUCCION) }
            }
        })
    }

    override fun hablar(
        texto: String,
        idioma: Idioma,
        alTerminar: () -> Unit,
        alFallar: (FalloNarrador) -> Unit
    ) {
        val peticion = Peticion("linea_${contador++}", texto, idioma, alTerminar, alFallar)
        when (estado) {
            Estado.INICIANDO -> pendiente = peticion
            Estado.LISTO -> ejecutar(peticion)
            Estado.ERROR -> principal.post { alFallar(FalloNarrador.MOTOR_NO_DISPONIBLE) }
            Estado.LIBERADO -> Unit
        }
    }

    override fun detener() {
        pendiente = null
        actual = null
        if (estado == Estado.LISTO) tts.stop()
    }

    override fun liberar() {
        detener()
        estado = Estado.LIBERADO
        tts.shutdown()
    }

    private fun alIniciar(status: Int) {
        if (estado == Estado.LIBERADO) return
        estado = if (status == TextToSpeech.SUCCESS) Estado.LISTO else Estado.ERROR
        Log.d(TAG, "Motor de voz iniciado: $estado")
        pendiente?.let {
            pendiente = null
            if (estado == Estado.LISTO) ejecutar(it) else it.alFallar(FalloNarrador.MOTOR_NO_DISPONIBLE)
        }
    }

    private fun ejecutar(peticion: Peticion) {
        if (peticion.texto.isBlank()) {
            principal.post { peticion.alTerminar() }
            return
        }
        if (peticion.idioma != idiomaConfigurado) {
            val resultado = tts.setLanguage(Locale.forLanguageTag(peticion.idioma.etiquetaLocale))
            if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
                peticion.alFallar(FalloNarrador.IDIOMA_NO_DISPONIBLE)
                return
            }
            idiomaConfigurado = peticion.idioma
        }
        actual = peticion
        if (tts.speak(peticion.texto, TextToSpeech.QUEUE_FLUSH, null, peticion.id) == TextToSpeech.ERROR) {
            actual = null
            peticion.alFallar(FalloNarrador.ERROR_REPRODUCCION)
        }
    }

    /** Devuelve la petición en curso si [utteranceId] es suya (ignora las ya detenidas) */
    private fun terminar(utteranceId: String?): Peticion? {
        val peticion = actual ?: return null
        if (peticion.id != utteranceId) return null
        actual = null
        return peticion
    }

    private companion object {
        const val TAG = "Narrador"
    }
}
