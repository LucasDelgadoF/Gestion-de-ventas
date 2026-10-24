package com.example.gestionventas.model

enum class Perfil { VENDEDOR, ENCARGADO }

/** Cuenta ficticia de prueba. */
data class Usuario(
    val username: String,
    val nombre: String,
    val perfil: Perfil,
    val sucursal: String
)
