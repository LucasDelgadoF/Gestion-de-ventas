package com.example.gestionventas.model

import com.example.gestionventas.data.local.VentaEntity

data class ComisionVendedor(
    val vendedor: String,
    val ventasValidadas: Int,
    val montoTotal: Double,
    val comision: Double
)

data class ResumenComisiones(
    val totalVentas: Int = 0,
    val pendientes: Int = 0,
    val observadas: Int = 0,
    val validadas: Int = 0,
    val rechazadas: Int = 0,
    val montoValidado: Double = 0.0,
    val comisionTotal: Double = 0.0,
    val porVendedor: List<ComisionVendedor> = emptyList(),
    val pendientesPrestador: List<VentaEntity> = emptyList()
)

/** Cálculo SIMULADO de comisiones (porcentajes ficticios definidos en Catalogo). */
object ComisionCalculator {
    fun calcular(ventas: List<VentaEntity>): ResumenComisiones {
        val validas = ventas.filter { it.estado == EstadoVenta.VALIDADA || it.estado == EstadoVenta.GESTIONADA }
        val porVendedor = validas.groupBy { it.vendedor }.map { (vendedor, lista) ->
            ComisionVendedor(
                vendedor = vendedor,
                ventasValidadas = lista.size,
                montoTotal = lista.sumOf { it.monto },
                comision = lista.sumOf { it.monto * Catalogo.porcentajeComision(it.tipoServicio) / 100.0 }
            )
        }.sortedBy { it.vendedor }
        return ResumenComisiones(
            totalVentas = ventas.size,
            pendientes = ventas.count { it.estado in EstadoVenta.PARA_REVISAR },
            observadas = ventas.count { it.estado == EstadoVenta.OBSERVADA },
            validadas = validas.size,
            rechazadas = ventas.count { it.estado == EstadoVenta.RECHAZADA },
            montoValidado = validas.sumOf { it.monto },
            comisionTotal = porVendedor.sumOf { it.comision },
            porVendedor = porVendedor,
            pendientesPrestador = ventas.filter { it.estado == EstadoVenta.VALIDADA }
        )
    }
}
