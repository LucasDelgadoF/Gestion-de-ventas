package com.example.gestionventas.data.repository

import com.example.gestionventas.model.Perfil
import com.example.gestionventas.model.Usuario
import java.security.MessageDigest

/**
 * Autenticación con cuentas FICTICIAS. Las claves no se guardan en texto visible:
 * se compara el hash SHA-256 de lo ingresado contra el hash almacenado.
 */
class AuthRepository {

    private data class Cuenta(val usuario: Usuario, val hashClave: String)

    private val cuentas = listOf(
        Cuenta(Usuario("vendedor1", "Vendedor Uno (ficticio)", Perfil.VENDEDOR, "Sucursal Puente Alto"),
            "7e6e1f7bd27296e97d8494fc51cb3d19355b26e062e7c09a163d954213f41ab9"),   // vend123
        Cuenta(Usuario("vendedor2", "Vendedor Dos (ficticio)", Perfil.VENDEDOR, "Sucursal Santiago"),
            "7e6e1f7bd27296e97d8494fc51cb3d19355b26e062e7c09a163d954213f41ab9"),   // vend123
        Cuenta(Usuario("encargado", "Encargado de Gestión (ficticio)", Perfil.ENCARGADO, "Casa Matriz"),
            "714266b5cc4dd5b34d0841a32d221fba895c2aecc099289a5c98f4e586b6ab0a")    // enc123
    )

    fun login(username: String, password: String): Usuario? {
        val cuenta = cuentas.firstOrNull { it.usuario.username == username } ?: return null
        return if (sha256(password) == cuenta.hashClave) cuenta.usuario else null
    }

    private fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
