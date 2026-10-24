package com.example.gestionventas.model

/**
 * Resultado de validar el formulario. null = campo válido,
 * texto = mensaje de error que debe mostrarse en el campo.
 */
data class VentaErrores(
    val tipoServicio: String? = null,
    val fecha: String? = null,
    val cliente: String? = null,
    val monto: String? = null,
    val codigo: String? = null,
    val respaldo: String? = null
) {
    fun hayErrores(): Boolean = listOfNotNull(tipoServicio, fecha, cliente, monto, codigo, respaldo).isNotEmpty()
}
