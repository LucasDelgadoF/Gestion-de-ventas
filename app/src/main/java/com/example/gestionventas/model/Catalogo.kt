package com.example.gestionventas.model

/** Catálogo ficticio de servicios, prestadores y porcentajes de comisión simulados. */
object Catalogo {
    val TIPOS = listOf(
        "Convenio dental",
        "Servicio automotriz",
        "Servicio veterinario",
        "Otros beneficios"
    )

    private val PRESTADORES = mapOf(
        TIPOS[0] to "Clínica Dental Ficticia",
        TIPOS[1] to "Taller Automotriz Ficticio",
        TIPOS[2] to "Veterinaria Ficticia",
        TIPOS[3] to "Prestador General Ficticio"
    )

    // Porcentajes SIMULADOS (el caso no define fórmula real).
    private val COMISION = mapOf(
        TIPOS[0] to 8.0,
        TIPOS[1] to 6.0,
        TIPOS[2] to 7.0,
        TIPOS[3] to 5.0
    )

    fun prestadorDe(tipo: String): String = PRESTADORES[tipo] ?: "Prestador General Ficticio"
    fun porcentajeComision(tipo: String): Double = COMISION[tipo] ?: 5.0
}
