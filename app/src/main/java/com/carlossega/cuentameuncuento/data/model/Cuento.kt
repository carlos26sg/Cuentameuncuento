package com.carlossega.cuentameuncuento.data.model

/**
 * Cuento tal y como aparece en el listado (título y descripción ya en el idioma pedido)
 */
data class Cuento(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val imagen: String
)

/**
 * Una línea del texto de un cuento.
 * En el fichero de Firestore cada línea tiene el formato `texto/numeroImagen`.
 */
data class LineaCuento(
    val texto: String,
    val imagen: String?
) {
    /** La última línea de cada cuento es "FIN", "FI" o "THE END" según el idioma */
    val esFinal: Boolean
        get() = texto.trim().uppercase() in MARCAS_FINAL

    companion object {
        private val MARCAS_FINAL = setOf("FIN", "FI", "THE END")
        private val MARCA_BOM = Char(0xFEFF)

        fun desdeTexto(linea: String): LineaCuento {
            val texto = linea.substringBefore("/").trim()
            val imagen = linea.substringAfter("/", "").substringBefore("/").trim()
            return LineaCuento(texto, imagen.ifEmpty { null })
        }

        /** Convierte el fichero completo en líneas, descartando las vacías */
        fun desdeFichero(contenido: String): List<LineaCuento> =
            contenido.trimStart(MARCA_BOM)
                .lineSequence()
                .filter { it.isNotBlank() }
                .map(::desdeTexto)
                .toList()
    }
}
