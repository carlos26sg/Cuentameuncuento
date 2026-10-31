package com.carlossega.cuentameuncuento.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.VelocidadVoz
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

enum class FalloNarrador { MOTOR_NO_DISPONIBLE, IDIOMA_NO_DISPONIBLE, ERROR_REPRODUCCION }

/**
 * Una frase para leer en voz alta. [pausaPreviaMs] es el silencio antes de empezar,
 * para que la narración respire entre frases.
 */
data class Locucion(
    val texto: String,
    val idioma: Idioma,
    val velocidad: VelocidadVoz = VelocidadVoz.NORMAL,
    val pausaPreviaMs: Long = 0
)

/**
 * Lee textos en voz alta. Los callbacks se llaman siempre en el hilo principal.
 * [alPalabra] recibe la posición (inicio, fin) en el texto de la palabra que suena,
 * si el motor de voz lo indica.
 */
interface Narrador {
    fun hablar(
        locucion: Locucion,
        alPalabra: (inicio: Int, fin: Int) -> Unit,
        alTerminar: () -> Unit,
        alFallar: (FalloNarrador) -> Unit
    )

    fun detener()
    fun liberar()
}

/**
 * [Narrador] con el TextToSpeech del sistema. Usa un único motor durante toda la vida
 * del objeto y, para cada idioma, la voz instalada de más calidad.
 */
class TtsNarrador @Inject constructor(
    @ApplicationContext context: Context
) : Narrador {

    private class Peticion(
        val id: String,
        val locucion: Locucion,
        val alPalabra: (Int, Int) -> Unit,
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

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                principal.post { actual?.takeIf { it.id == utteranceId }?.alPalabra?.invoke(start, end) }
            }

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
        locucion: Locucion,
        alPalabra: (inicio: Int, fin: Int) -> Unit,
        alTerminar: () -> Unit,
        alFallar: (FalloNarrador) -> Unit
    ) {
        val peticion = Peticion("linea_${contador++}", locucion, alPalabra, alTerminar, alFallar)
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
        val locucion = peticion.locucion
        if (locucion.texto.isBlank()) {
            principal.post { peticion.alTerminar() }
            return
        }
        if (locucion.idioma != idiomaConfigurado) {
            if (!configurarIdioma(locucion.idioma)) {
                peticion.alFallar(FalloNarrador.IDIOMA_NO_DISPONIBLE)
                return
            }
            idiomaConfigurado = locucion.idioma
        }
        tts.setSpeechRate(locucion.velocidad.factor)
        tts.setPitch(TONO)
        actual = peticion

        // El silencio va en la misma cola que la frase, así detener() corta los dos
        var cola = TextToSpeech.QUEUE_FLUSH
        if (locucion.pausaPreviaMs > 0) {
            tts.playSilentUtterance(locucion.pausaPreviaMs, cola, "${peticion.id}_pausa")
            cola = TextToSpeech.QUEUE_ADD
        }
        if (tts.speak(locucion.texto, cola, null, peticion.id) == TextToSpeech.ERROR) {
            actual = null
            peticion.alFallar(FalloNarrador.ERROR_REPRODUCCION)
        }
    }

    private fun configurarIdioma(idioma: Idioma): Boolean {
        val locale = Locale.forLanguageTag(idioma.etiquetaLocale)
        val resultado = tts.setLanguage(locale)
        if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
            return false
        }
        mejorVoz(locale)?.let {
            Log.d(TAG, "Voz elegida para ${idioma.etiquetaLocale}: ${it.name}")
            tts.voice = it
        }
        return true
    }

    /**
     * La voz por defecto del idioma no siempre es la mejor que hay instalada. Se elige,
     * entre las que funcionan sin conexión, la del mismo país y de más calidad.
     */
    private fun mejorVoz(locale: Locale): Voice? {
        val voces = try {
            tts.voices.orEmpty()
        } catch (e: RuntimeException) {
            // Algunos motores de voz antiguos fallan al listar las voces
            Log.w(TAG, "No se pudieron listar las voces", e)
            emptySet()
        }
        return voces
            .filter { it.locale.language == locale.language }
            .filter { !it.isNetworkConnectionRequired }
            .filter { TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }
            .maxWithOrNull(
                compareBy<Voice>({ it.locale.country == locale.country }, { it.quality }, { -it.latency })
            )
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

        // Un poco más agudo que el normal: suena más cercano para un cuento infantil
        const val TONO = 1.05f
    }
}
