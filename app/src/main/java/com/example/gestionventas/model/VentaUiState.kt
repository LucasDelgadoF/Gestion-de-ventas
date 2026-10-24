package com.example.gestionventas.model

/**
 * Estado completo de la pantalla del formulario de venta (VentaFormScreen).
 * Inmutable: el ViewModel lo actualiza con update { it.copy(...) } y la UI lo observa por StateFlow.
 */
data class VentaUiState(
    val id: Long = 0L,                       // 0 = venta nueva
    val tipoServicio: String = "",
    val fechaOperacion: String = "",
    val cliente: String = "",
    val monto: String = "",
    val codigoAutorizacion: String = "",
    val respaldoUri: String? = null,
    val estado: String = EstadoVenta.BORRADOR,
    val observaciones: String = "",
    val observacionRevision: String = "",    // observación dejada por el encargado
    val editable: Boolean = true,            // false = solo lectura (venta validada, etc.)

    // Errores por campo (null = válido)
    val tipoServicioError: String? = null,
    val fechaError: String? = null,
    val clienteError: String? = null,
    val montoError: String? = null,
    val codigoError: String? = null,
    val respaldoError: String? = null,

    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false
)
