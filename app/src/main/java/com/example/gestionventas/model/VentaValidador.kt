package com.example.gestionventas.model

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Reglas de validación puras (sin Compose, sin Android UI): se pueden probar con tests unitarios.
 * El ViewModel las invoca; la pantalla solo muestra el resultado.
 */
object VentaValidador {

    private val CODIGO_REGEX = Regex("^[A-Za-z0-9-]{4,20}$")

    fun validarTipo(tipo: String): String? =
        if (tipo !in Catalogo.TIPOS) "Seleccione un tipo de convenio o servicio" else null

    fun validarFecha(fecha: String): String? {
        if (fecha.isBlank()) return "La fecha de operación es obligatoria"
        if (!Regex("^\\d{2}/\\d{2}/\\d{4}$").matches(fecha)) return "Formato inválido (dd/MM/aaaa)"
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }
        return try {
            val d = sdf.parse(fecha)
            if (d != null && d.time > System.currentTimeMillis() + 24L * 3600 * 1000) "La fecha no puede ser futura" else null
        } catch (e: Exception) {
            "Fecha inexistente"
        }
    }

    fun validarCliente(cliente: String): String? = when {
        cliente.isBlank() -> "El cliente ficticio es obligatorio"
        cliente.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        else -> null
    }

    fun validarMonto(monto: String): String? {
        if (monto.isBlank()) return "El monto es obligatorio"
        val valor = monto.replace(",", ".").toDoubleOrNull() ?: return "Ingrese un número válido"
        return if (valor <= 0.0) "El monto debe ser mayor que 0" else null
    }

    fun validarCodigo(codigo: String): String? = when {
        codigo.isBlank() -> "El código de autorización es obligatorio"
        !CODIGO_REGEX.matches(codigo.trim()) -> "Use 4 a 20 letras, números o guiones"
        else -> null
    }

    fun validarRespaldo(uri: String?): String? =
        if (uri.isNullOrBlank()) "Adjunte un respaldo fotográfico ficticio para enviar" else null

    /** Validación completa. El respaldo solo es obligatorio al enviar a revisión. */
    fun validar(
        tipo: String, fecha: String, cliente: String, monto: String,
        codigo: String, respaldoUri: String?, exigirRespaldo: Boolean
    ) = VentaErrores(
        tipoServicio = validarTipo(tipo),
        fecha = validarFecha(fecha),
        cliente = validarCliente(cliente),
        monto = validarMonto(monto),
        codigo = validarCodigo(codigo),
        respaldo = if (exigirRespaldo) validarRespaldo(respaldoUri) else null
    )
}
