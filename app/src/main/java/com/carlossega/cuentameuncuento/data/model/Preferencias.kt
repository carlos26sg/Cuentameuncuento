package com.carlossega.cuentameuncuento.data.model

import androidx.annotation.DrawableRes
import com.carlossega.cuentameuncuento.R

/**
 * Idiomas en los que hay cuentos. `codigo` es el sufijo que usan los campos de Firestore
 * (titulo_esp, txt_cat...), así que no se puede cambiar sin migrar los datos.
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
 * Modo de uso de un cuento. `codigo` es el valor guardado en Firestore (campo modo_fav).
 */
enum class Modo(val codigo: String) {
    LEER("leer"),
    ESCUCHAR("reproducir");

    companion object {
        fun desdeCodigo(codigo: String?): Modo? = entries.firstOrNull { it.codigo == codigo }
    }
}

/**
 * Preferencias del usuario. Se guardan en local (DataStore) y se sincronizan con Firestore.
 */
data class Preferencias(
    val nombre: String = "",
    val idioma: Idioma = Idioma.ESPANOL,
    val favorito: String = "",
    val modoFavorito: Modo? = null
) {
    val tieneFavorito: Boolean
        get() = favorito.isNotEmpty() && modoFavorito != null
}
