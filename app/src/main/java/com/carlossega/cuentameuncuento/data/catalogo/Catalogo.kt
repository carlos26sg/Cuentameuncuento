package com.carlossega.cuentameuncuento.data.catalogo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Todos los cuentos en un único fichero (`catalogo.json`), generado por
 * `herramientas/contenido.py` a partir de la carpeta `contenido/` del repositorio.
 * Las rutas de las imágenes son relativas a la carpeta `contenido/` y llevan un resumen
 * de su contenido en el nombre: si una imagen cambia, cambia también su ruta.
 */
@Serializable
data class Catalogo(
    val formato: Int,
    /** Segundos desde 1970; entre dos catálogos gana el más reciente */
    val generado: Long,
    val revision: String,
    val cuentos: List<CuentoCatalogo>
) {
    /** Rutas de todas las imágenes que cita el catálogo */
    val rutasImagenes: Set<String>
        get() = cuentos.flatMapTo(mutableSetOf()) { it.ilustraciones.values + it.portada }

    companion object {
        /** Versión del formato que entiende esta app */
        const val FORMATO = 1

        private val json = Json { ignoreUnknownKeys = true }

        /** Devuelve null si el texto no es un catálogo válido para esta versión de la app */
        fun leer(texto: String): Catalogo? = try {
            json.decodeFromString<Catalogo>(texto).takeIf { it.formato == FORMATO && it.cuentos.isNotEmpty() }
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}

/** Títulos, descripciones y textos indexados por código de idioma (esp, cat, eng) */
@Serializable
data class CuentoCatalogo(
    val id: String,
    val edad: Int? = null,
    val titulo: Map<String, String>,
    val descripcion: Map<String, String>,
    val portada: String,
    /** Número de ilustración → ruta */
    val ilustraciones: Map<String, String>,
    val lineas: List<LineaCatalogo>
)

/** Una línea en todos los idiomas a la vez: la traducción siempre va alineada */
@Serializable
data class LineaCatalogo(
    val imagen: String? = null,
    val texto: Map<String, String>
)
