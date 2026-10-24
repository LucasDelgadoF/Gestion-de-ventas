package com.example.gestionventas.ui.venta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gestionventas.data.local.VentaEntity
import com.example.gestionventas.model.EstadoVenta

/** Módulo de revisión del Encargado: ver antecedentes, respaldo, historial y validar/observar/rechazar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRevisionScreen(
    navController: NavController,
    vm: VentaViewModel,
    ventaId: Long
) {
    LaunchedEffect(ventaId) { vm.seleccionarVenta(ventaId) }

    val venta by vm.ventaSeleccionada.collectAsState()
    val historial by vm.historial.collectAsState()
    val usuario by vm.usuario.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var observacion by remember { mutableStateOf("") }
    var errorObservacion by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisión de venta") },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val v: VentaEntity? = venta
        if (v == null) {
            Text("Cargando venta...", modifier = Modifier.padding(innerPadding).padding(16.dp))
            return@Scaffold
        }
        val esPropia = usuario?.username == v.vendedor
        val puedeRevisar = !esPropia && v.estado in EstadoVenta.PARA_REVISAR

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(v.codigoAutorizacion, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                EstadoChip(v.estado)
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Fila("Vendedor", v.vendedor)
                    Fila("Cliente (ficticio)", v.cliente)
                    Fila("Servicio", v.tipoServicio)
                    Fila("Prestador", v.prestador)
                    Fila("Fecha operación", v.fechaOperacion)
                    Fila("Monto", formatoMonto(v.monto))
                    if (v.observaciones.isNotBlank()) Fila("Observaciones del vendedor", v.observaciones)
                    if (v.revisadoPor != null) Fila("Última revisión por", v.revisadoPor)
                    if (v.observacionRevision.isNotBlank()) Fila("Observación de revisión", v.observacionRevision)
                }
            }

            Text("Respaldo adjunto", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            RespaldoImage(v.respaldoUri)

            // ---------- Acciones de revisión ----------
            if (esPropia) {
                Text("No puede validar sus propias ventas.", color = MaterialTheme.colorScheme.error)
            }
            if (puedeRevisar) {
                CampoValidado(
                    value = observacion,
                    onValueChange = { observacion = it; errorObservacion = null },
                    label = "Observación / motivo (obligatorio al observar o rechazar)",
                    icono = Icons.Default.Notes,
                    error = errorObservacion,
                    singleLine = false,
                    minLines = 2
                )
                val manejarError: (String) -> Unit = { errorObservacion = it }
                Button(
                    onClick = { vm.revisar(v, EstadoVenta.VALIDADA, observacion, manejarError) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Validar venta")
                }
                OutlinedButton(
                    onClick = { vm.revisar(v, EstadoVenta.OBSERVADA, observacion, manejarError) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Solicitar corrección")
                }
                Button(
                    onClick = { vm.revisar(v, EstadoVenta.RECHAZADA, observacion, manejarError) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Rechazar venta")
                }
            }
            AnimatedVisibility(visible = v.estado == EstadoVenta.VALIDADA) {
                Button(onClick = { vm.gestionarConPrestador(v) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Derivar a prestador (simulado)")
                }
            }

            // ---------- Historial / trazabilidad ----------
            HorizontalDivider()
            Text("Historial de cambios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            historial.forEach { h ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Text(h.accion, fontWeight = FontWeight.Bold)
                        Text("${h.estadoAnterior} → ${h.estadoNuevo}", style = MaterialTheme.typography.bodySmall)
                        Text("${h.usuario} · ${formatoFechaHora(h.fechaHora)}", style = MaterialTheme.typography.bodySmall)
                        if (h.observacion.isNotBlank()) Text("“${h.observacion}”", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.45f))
        Text(valor, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.55f))
    }
}
