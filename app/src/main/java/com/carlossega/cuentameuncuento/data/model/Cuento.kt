package com.carlossega.cuentameuncuento.data.model

/**
 * Cuento tal y como aparece en la biblioteca (título y descripción ya en el idioma pedido)
 */
data class Cuento(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val imagen: String,
    /** Edad recomendada, "a partir de" */
    val edad: Int? = null,
    val paginas: Int = 0,
    /** Tiempo aproximado contándolo en voz alta */
    val minutos: Int = 0
)

/**
 * Una línea del texto de un cuento. [imagen] es el número de la ilustración que empieza
 * en esta línea (null si sigue la de la línea anterior).
 */
data class LineaCuento(
    val texto: String,
    val imagen: String?
) {
    /** La última línea de cada cuento es "FIN", "FI" o "THE END" según el idioma */
    val esFinal: Boolean
        get() = texto.trim().uppercase() in MARCAS_FINAL

    private companion object {
        val MARCAS_FINAL = setOf("FIN", "FI", "THE END")
    }
}
