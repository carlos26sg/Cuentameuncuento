package com.carlossega.cuentameuncuento

/**
 * Data class para items del Spinner con ID y nombre
 * Compatible con el código Java existente usando @JvmField
 */
data class SpinnerId(
    @JvmField var id: String = "",
    @JvmField var nombre: String = ""
) {
    override fun toString(): String = id
}
