package com.carlossega.cuentameuncuento.data.model

import androidx.annotation.FontRes
import com.carlossega.cuentameuncuento.R

/**
 * Idiomas en los que hay cuentos. `codigo` es la clave del idioma en catalogo.json y en
 * las preferencias guardadas, así que no se puede cambiar sin migrar los datos.
 */
enum class Idioma(val codigo: String, val etiquetaLocale: String) {
    ESPANOL("esp", "es-ES"),
    CATALAN("cat", "ca-ES"),
    INGLES("eng", "en-GB"),
    FRANCES("fra", "fr-FR"),
    ALEMAN("deu", "de-DE"),
    GALLEGO("glg", "gl-ES"),
    EUSKERA("eus", "eu-ES");

    companion object {
        fun desdeCodigo(codigo: String?): Idioma =
            entries.firstOrNull { it.codigo == codigo } ?: ESPANOL

        /** El idioma del móvil si hay cuentos en él; si no, español */
        fun delSistema(idiomaSistema: String = java.util.Locale.getDefault().language): Idioma =
            entries.firstOrNull { java.util.Locale.forLanguageTag(it.etiquetaLocale).language == idiomaSistema } ?: ESPANOL
    }
}

/**
 * Velocidad del narrador. `factor` es el que recibe TextToSpeech (1.0 = velocidad del sistema);
 * la normal es algo más lenta que la del sistema para que los niños la sigan bien.
 */
enum class VelocidadVoz(val codigo: String, val factor: Float) {
    LENTA("lenta", 0.75f),
    NORMAL("normal", 0.9f),
    RAPIDA("rapida", 1.05f);

    companion object {
        fun desdeCodigo(codigo: String?): VelocidadVoz =
            entries.firstOrNull { it.codigo == codigo } ?: NORMAL
    }
}

/**
 * Letra del texto de los cuentos. Andika está pensada para niños que aprenden a leer;
 * Anna es la letra manuscrita de los títulos y botones de la app.
 */
enum class LetraCuento(val codigo: String, @FontRes val fuente: Int, val nombre: String) {
    ANDIKA("andika", R.font.andika, "Andika"),
    ATKINSON("atkinson", R.font.atkinson, "Atkinson Hyperlegible"),
    ANNA("anna", R.font.anna, "Anna");

    companion object {
        fun desdeCodigo(codigo: String?): LetraCuento =
            entries.firstOrNull { it.codigo == codigo } ?: ANDIKA
    }
}

/** Tamaño del texto de los cuentos (botón "Aa" del lector y Ajustes) */
enum class TamanoTexto(val codigo: String, val factor: Float) {
    NORMAL("normal", 1f),
    GRANDE("grande", 1.2f),
    MUY_GRANDE("muy_grande", 1.4f);

    /** El botón "Aa" va pasando por los tamaños y vuelve al primero */
    val siguiente: TamanoTexto get() = entries[(ordinal + 1) % entries.size]

    companion object {
        fun desdeCodigo(codigo: String?): TamanoTexto =
            entries.firstOrNull { it.codigo == codigo } ?: NORMAL
    }
}

/**
 * Preferencias de la app, guardadas solo en el dispositivo (DataStore)
 */
data class Preferencias(
    val idioma: Idioma = Idioma.ESPANOL,
    val favoritos: Set<String> = emptySet(),
    val velocidadVoz: VelocidadVoz = VelocidadVoz.NORMAL,
    val letra: LetraCuento = LetraCuento.ANDIKA,
    val tamanoTexto: TamanoTexto = TamanoTexto.NORMAL
)

/** Cuento que se ha dejado a medias, para ofrecer "Seguir leyendo" */
data class Lectura(
    val cuentoId: String,
    val idioma: Idioma,
    val linea: Int
)
