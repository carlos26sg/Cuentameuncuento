package com.carlossega.cuentameuncuento.data.model

import androidx.annotation.DrawableRes
import com.carlossega.cuentameuncuento.R

/**
 * Idiomas en los que hay cuentos. `codigo` es la clave del idioma en catalogo.json y en
 * las preferencias guardadas, así que no se puede cambiar sin migrar los datos.
 */
enum class Idioma(val codigo: String, val etiquetaLocale: String, @DrawableRes val bandera: Int) {
    ESPANOL("esp", "es-ES", R.drawable.espanol),
    CATALAN("cat", "ca-ES", R.drawable.catalan),
    INGLES("eng", "en-GB", R.drawable.ingles);

    companion object {
        fun desdeCodigo(codigo: String?): Idioma =
            entries.firstOrNull { it.codigo == codigo } ?: ESPANOL
    }
}

/**
 * Modo de uso de un cuento. `codigo` es el valor que se guarda en las preferencias.
 */
enum class Modo(val codigo: String) {
    LEER("leer"),
    ESCUCHAR("reproducir");

    companion object {
        fun desdeCodigo(codigo: String?): Modo? = entries.firstOrNull { it.codigo == codigo }
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
 * Preferencias de la app, guardadas solo en el dispositivo (DataStore)
 */
data class Preferencias(
    val idioma: Idioma = Idioma.ESPANOL,
    val favorito: String = "",
    val modoFavorito: Modo? = null,
    val velocidadVoz: VelocidadVoz = VelocidadVoz.NORMAL
) {
    val tieneFavorito: Boolean
        get() = favorito.isNotEmpty() && modoFavorito != null
}
