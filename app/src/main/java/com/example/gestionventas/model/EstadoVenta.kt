package com.example.gestionventas.model

/** Estados de la venta (subconjunto del caso, suficiente para el flujo de revisión). */
object EstadoVenta {
    const val BORRADOR = "Borrador"
    const val PENDIENTE = "Pendiente de revisión"
    const val OBSERVADA = "Observada"
    const val CORREGIDA = "Corregida"
    const val VALIDADA = "Validada"
    const val RECHAZADA = "Rechazada"
    const val GESTIONADA = "Gestionada con el prestador"

    val TODOS = listOf(BORRADOR, PENDIENTE, OBSERVADA, CORREGIDA, VALIDADA, RECHAZADA, GESTIONADA)

    /** Estados que el encargado debe revisar. */
    val PARA_REVISAR = listOf(PENDIENTE, CORREGIDA)

    /** El vendedor puede modificar mientras no esté validada ni cerrada. */
    fun esEditable(estado: String): Boolean =
        estado == BORRADOR || estado == OBSERVADA || estado == PENDIENTE || estado == CORREGIDA
}
