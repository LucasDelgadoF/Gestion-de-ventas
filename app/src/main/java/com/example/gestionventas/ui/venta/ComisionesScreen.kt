package com.example.gestionventas.ui.venta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/** Indicadores generales y comisiones SIMULADAS (porcentajes ficticios del catálogo). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComisionesScreen(navController: NavController, vm: VentaViewModel) {
    val resumen by vm.resumen.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Indicadores y comisiones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Indicadores generales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Indicador("Total", resumen.totalVentas.toString(), Modifier.weight(1f))
                Indicador("Pendientes", resumen.pendientes.toString(), Modifier.weight(1f))
                Indicador("Observadas", resumen.observadas.toString(), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Indicador("Validadas", resumen.validadas.toString(), Modifier.weight(1f))
                Indicador("Rechazadas", resumen.rechazadas.toString(), Modifier.weight(1f))
                Indicador("Monto validado", formatoMonto(resumen.montoValidado), Modifier.weight(1f))
            }

            Text("Comisiones simuladas por vendedor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (resumen.porVendedor.isEmpty()) {
                Text("Aún no hay ventas validadas.")
            }
            resumen.porVendedor.forEach { c ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(c.vendedor, fontWeight = FontWeight.Bold)
                        Text("Ventas validadas: ${c.ventasValidadas}")
                        Text("Monto: ${formatoMonto(c.montoTotal)}")
                        Text("Comisión ficticia: ${formatoMonto(c.comision)}", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text("Comisión total simulada: ${formatoMonto(resumen.comisionTotal)}", fontWeight = FontWeight.Bold)

            Text("Pendientes de gestión con prestador", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (resumen.pendientesPrestador.isEmpty()) Text("No hay ventas pendientes de derivar.")
            resumen.pendientesPrestador.forEach { v ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${v.codigoAutorizacion} → ${v.prestador}", fontWeight = FontWeight.Medium)
                        Text("${v.tipoServicio} · ${formatoMonto(v.monto)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Text(
                "Los porcentajes y montos son ficticios y no producen efectos administrativos ni financieros.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Indicador(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelSmall)
            Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
