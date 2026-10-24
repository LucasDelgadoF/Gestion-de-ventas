package com.example.gestionventas.data.repository

import androidx.room.withTransaction
import com.example.gestionventas.data.local.AppDatabase
import com.example.gestionventas.data.local.HistorialEntity
import com.example.gestionventas.data.local.VentaDao
import com.example.gestionventas.data.local.VentaEntity
import com.example.gestionventas.model.EstadoVenta
import com.example.gestionventas.model.Catalogo
import kotlinx.coroutines.flow.Flow

/**
 * Única fuente de datos de ventas para el ViewModel.
 * Toda escritura de una venta deja además un registro de historial (trazabilidad),
 * dentro de una transacción para que ambos queden consistentes.
 */
class VentaRepository(private val db: AppDatabase) {

    private val dao: VentaDao = db.ventaDao()

    fun todasLasVentas(): Flow<List<VentaEntity>> = dao.observarTodas()
    fun ventasDeVendedor(vendedor: String): Flow<List<VentaEntity>> = dao.observarPorVendedor(vendedor)
    fun observarVenta(id: Long): Flow<VentaEntity?> = dao.observarPorId(id)
    fun observarHistorial(ventaId: Long): Flow<List<HistorialEntity>> = dao.observarHistorial(ventaId)

    suspend fun obtenerVenta(id: Long): VentaEntity? = dao.obtenerPorId(id)

    suspend fun existeCodigo(codigo: String, excluirId: Long): Boolean =
        dao.contarCodigo(codigo, excluirId) > 0

    /** Inserta una venta nueva y registra su creación en el historial. */
    suspend fun crearVenta(venta: VentaEntity, usuario: String, accion: String, observacion: String = ""): Long =
        db.withTransaction {
            val id = dao.insertar(venta)
            dao.insertarHistorial(
                HistorialEntity(
                    ventaId = id,
                    estadoAnterior = "-",
                    estadoNuevo = venta.estado,
                    accion = accion,
                    usuario = usuario,
                    observacion = observacion
                )
            )
            id
        }

    /** Actualiza una venta existente registrando el cambio (estado anterior -> nuevo). */
    suspend fun actualizarVenta(
        anterior: VentaEntity,
        nueva: VentaEntity,
        usuario: String,
        accion: String,
        observacion: String = ""
    ) {
        db.withTransaction {
            dao.actualizar(nueva.copy(fechaActualizacion = System.currentTimeMillis()))
            dao.insertarHistorial(
                HistorialEntity(
                    ventaId = nueva.id,
                    estadoAnterior = anterior.estado,
                    estadoNuevo = nueva.estado,
                    accion = accion,
                    usuario = usuario,
                    observacion = observacion
                )
            )
        }
    }

    suspend fun eliminarVenta(venta: VentaEntity) = dao.eliminar(venta)

    /** Datos 100% ficticios para poder demostrar la app sin ingresar nada. */
    suspend fun sembrarDatosDePrueba() {
        if (dao.contarVentas() > 0) return
        data class Semilla(
            val tipo: String, val fecha: String, val cliente: String, val monto: Double,
            val codigo: String, val estado: String, val vendedor: String, val obs: String = ""
        )
        val semillas = listOf(
            Semilla(Catalogo.TIPOS[0], "05/10/2026", "Cliente Ficticio Uno", 45000.0, "AUT-1001", EstadoVenta.BORRADOR, "vendedor1"),
            Semilla(Catalogo.TIPOS[1], "06/10/2026", "Cliente Ficticio Dos", 78000.0, "AUT-1002", EstadoVenta.PENDIENTE, "vendedor1"),
            Semilla(Catalogo.TIPOS[2], "07/10/2026", "Cliente Ficticio Tres", 32000.0, "AUT-1003", EstadoVenta.OBSERVADA, "vendedor1",
                "Respaldo poco legible, volver a adjuntar."),
            Semilla(Catalogo.TIPOS[0], "08/10/2026", "Cliente Ficticio Cuatro", 51000.0, "AUT-2001", EstadoVenta.PENDIENTE, "vendedor2"),
            Semilla(Catalogo.TIPOS[3], "09/10/2026", "Cliente Ficticio Cinco", 29000.0, "AUT-2002", EstadoVenta.VALIDADA, "vendedor2")
        )
        semillas.forEach { s ->
            crearVenta(
                VentaEntity(
                    tipoServicio = s.tipo,
                    prestador = Catalogo.prestadorDe(s.tipo),
                    fechaOperacion = s.fecha,
                    cliente = s.cliente,
                    monto = s.monto,
                    codigoAutorizacion = s.codigo,
                    estado = s.estado,
                    vendedor = s.vendedor,
                    observacionRevision = s.obs,
                    revisadoPor = if (s.estado == EstadoVenta.OBSERVADA || s.estado == EstadoVenta.VALIDADA) "encargado" else null
                ),
                usuario = s.vendedor,
                accion = "Registro de datos de prueba"
            )
        }
    }
}
