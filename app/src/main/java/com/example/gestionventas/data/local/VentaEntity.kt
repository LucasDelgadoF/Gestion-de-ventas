package com.example.gestionventas.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla de ventas. El código de autorización es único (índice) para impedir
 * la duplicación evidente de códigos, tanto a nivel de lógica como de base de datos.
 */
@Entity(
    tableName = "ventas",
    indices = [Index(value = ["codigoAutorizacion"], unique = true)]
)
data class VentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipoServicio: String,
    val prestador: String,
    val fechaOperacion: String,
    val cliente: String,
    val monto: Double,
    val codigoAutorizacion: String,
    val respaldoUri: String? = null,
    val estado: String = "Borrador",
    val observaciones: String = "",
    val vendedor: String,
    val observacionRevision: String = "",
    val revisadoPor: String? = null,
    val fechaActualizacion: Long = System.currentTimeMillis()
)
