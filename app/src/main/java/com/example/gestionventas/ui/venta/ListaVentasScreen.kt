package com.example.gestionventas.ui.venta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gestionventas.data.local.VentaEntity
import com.example.gestionventas.model.EstadoVenta
import com.example.gestionventas.model.Perfil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaVentasScreen(
    navController: NavController,
    vm: VentaViewModel
) {
    val ventas by vm.ventas.collectAsState()
    val usuario by vm.usuario.collectAsState()
    val filtros by vm.filtros.collectAsState()
    val vendedores by vm.vendedoresDisponibles.collectAsState()
    val resumen by vm.resumen.collectAsState()
    val mensaje by vm.mensaje.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarFiltros by remember { mutableStateOf(false) }

    val esEncargado = usuario?.perfil == Perfil.ENCARGADO

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (esEncargado) "Ventas recibidas" else "Mis ventas")
                        Text(
                            usuario?.nombre ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { mostrarFiltros = !mostrarFiltros }) {
                        Icon(Icons.Default.FilterAlt, contentDescription = "Filtros")
                    }
                    IconButton(onClick = { navController.navigate("comisiones") }) {
                        Icon(Icons.Default.QueryStats, contentDescription = "Indicadores y comisiones")
                    }
                    IconButton(onClick = {
                        vm.cerrarSesion()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!esEncargado) {
                FloatingActionButton(onClick = {
                    vm.nuevaVenta()
                    navController.navigate("form")
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Registrar nueva venta")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // Alerta interna según el perfil
            val textoAlerta = if (esEncargado) {
                if (resumen.pendientes > 0) "${resumen.pendientes} venta(s) pendientes de revisión" else null
            } else {
                if (resumen.observadas > 0) "${resumen.observadas} venta(s) observadas: requieren corrección" else null
            }
            AnimatedVisibility(visible = textoAlerta != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFEF6C00))
                        Text(textoAlerta ?: "", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Búsqueda por código (siempre visible)
            CampoValidado(
                value = filtros.busqueda,
                onValueChange = vm::onBusquedaChange,
                label = "Buscar por código de autorización",
                icono = Icons.Default.Search,
                error = null,
                modifier = Modifier.padding(top = 8.dp)
            )

            AnimatedVisibility(visible = mostrarFiltros) {
                Column {
                    Text("Estado", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val estados = if (esEncargado) EstadoVenta.TODOS.filter { it != EstadoVenta.BORRADOR } else EstadoVenta.TODOS
                        items(estados) { e ->
                            FilterChip(
                                selected = filtros.estado == e,
                                onClick = { vm.onFiltroEstado(if (filtros.estado == e) null else e) },
                                label = { Text(e) }
                            )
                        }
                    }
                    if (esEncargado) {
                        Text("Vendedor", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(vendedores) { v ->
                                FilterChip(
                                    selected = filtros.vendedor == v,
                                    onClick = { vm.onFiltroVendedor(if (filtros.vendedor == v) null else v) },
                                    label = { Text(v) },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) }
                                )
                            }
                        }
                    }
                    CampoValidado(
                        value = filtros.fecha,
                        onValueChange = vm::onFiltroFecha,
                        label = "Fecha (ej: 07/10/2026)",
                        icono = Icons.Default.CalendarMonth,
                        error = null,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (ventas.isEmpty()) {
                Text(
                    text = if (esEncargado) "No hay ventas que coincidan con los filtros."
                    else "Aún no hay ventas. Use el botón + para registrar una.",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(ventas, key = { it.id }) { venta ->
                    VentaCard(
                        venta = venta,
                        mostrarVendedor = esEncargado,
                        puedeEliminar = !esEncargado && venta.estado == EstadoVenta.BORRADOR,
                        onClick = {
                            if (esEncargado) {
                                navController.navigate("revision/${venta.id}")
                            } else {
                                vm.cargarVenta(venta.id)
                                navController.navigate("form")
                            }
                        },
                        onEliminar = { vm.eliminarBorrador(venta) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun VentaCard(
    venta: VentaEntity,
    mostrarVendedor: Boolean,
    puedeEliminar: Boolean,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(venta.codigoAutorizacion, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                EstadoChip(venta.estado)
            }
            Text(venta.tipoServicio, style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Monto: ${formatoMonto(venta.monto)}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${venta.fechaOperacion}" + if (mostrarVendedor) " · ${venta.vendedor}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (puedeEliminar) {
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar borrador", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
