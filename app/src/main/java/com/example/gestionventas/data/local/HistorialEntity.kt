package com.example.gestionventas.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Trazabilidad: cada cambio relevante de una venta deja un registro aquí. */
@Entity(tableName = "historial", indices = [Index("ventaId")])
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ventaId: Long,
    val estadoAnterior: String,
    val estadoNuevo: String,
    val accion: String,
    val usuario: String,
    val fechaHora: Long = System.currentTimeMillis(),
    val observacion: String = ""
)
