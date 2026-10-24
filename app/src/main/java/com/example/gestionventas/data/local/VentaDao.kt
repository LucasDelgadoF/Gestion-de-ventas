package com.example.gestionventas.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {

    // ---------- Ventas ----------
    @Insert
    suspend fun insertar(venta: VentaEntity): Long

    @Update
    suspend fun actualizar(venta: VentaEntity)

    @Delete
    suspend fun eliminar(venta: VentaEntity)

    @Query("SELECT * FROM ventas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): VentaEntity?

    @Query("SELECT * FROM ventas WHERE id = :id")
    fun observarPorId(id: Long): Flow<VentaEntity?>

    @Query("SELECT * FROM ventas ORDER BY fechaActualizacion DESC")
    fun observarTodas(): Flow<List<VentaEntity>>

    @Query("SELECT * FROM ventas WHERE vendedor = :vendedor ORDER BY fechaActualizacion DESC")
    fun observarPorVendedor(vendedor: String): Flow<List<VentaEntity>>

    /** Cuenta ventas con el mismo código (excluyendo la que se está editando). */
    @Query("SELECT COUNT(*) FROM ventas WHERE codigoAutorizacion = :codigo AND id != :excluirId")
    suspend fun contarCodigo(codigo: String, excluirId: Long): Int

    @Query("SELECT COUNT(*) FROM ventas")
    suspend fun contarVentas(): Int

    // ---------- Historial ----------
    @Insert
    suspend fun insertarHistorial(historial: HistorialEntity)

    @Query("SELECT * FROM historial WHERE ventaId = :ventaId ORDER BY fechaHora DESC, id DESC")
    fun observarHistorial(ventaId: Long): Flow<List<HistorialEntity>>
}
