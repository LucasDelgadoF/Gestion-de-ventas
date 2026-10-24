package com.example.gestionventas.ui.venta

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gestionventas.camera.ImageStorage
import com.example.gestionventas.model.Catalogo
import com.example.gestionventas.model.EstadoVenta
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaFormScreen(
    navController: NavController,
    vm: VentaViewModel
) {
    val estado by vm.estado.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var menuTipo by remember { mutableStateOf(false) }
    var mostrarFecha by remember { mutableStateOf(false) }

    // Al guardar con éxito se vuelve a la lista (que se actualiza sola desde Room)
    LaunchedEffect(estado.guardadoExitosamente) {
        if (estado.guardadoExitosamente) {
            vm.consumirGuardado()
            navController.popBackStack()
        }
    }

    // Recurso nativo 2: selección de imagen desde el dispositivo (tipo y tamaño restringidos)
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val (ruta, error) = ImageStorage.importar(context, uri)
            if (ruta != null) vm.onRespaldoChange(ruta)
            else scope.launch { snackbarHostState.showSnackbar(error ?: "Error al importar la imagen") }
        }
    }

    val esNueva = estado.id == 0L
    val titulo = when {
        !estado.editable -> "Detalle de la venta"
        esNueva -> "Nueva venta"
        else -> "Modificar venta"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Estado:", fontWeight = FontWeight.Bold)
                EstadoChip(estado.estado)
            }

            // Observación del encargado (venta observada)
            AnimatedVisibility(visible = estado.observacionRevision.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Observación del encargado", fontWeight = FontWeight.Bold)
                        Text(estado.observacionRevision)
                    }
                }
            }

            // Tipo de convenio / servicio (lista desplegable)
            Box(Modifier.fillMaxWidth()) {
                CampoValidado(
                    value = estado.tipoServicio,
                    onValueChange = {},
                    label = "Tipo de convenio o servicio",
                    icono = Icons.Default.Category,
                    error = estado.tipoServicioError,
                    readOnly = true,
                    enabled = estado.editable,
                    trailing = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Desplegar") }
                )
                if (estado.editable) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .padding(bottom = 22.dp)
                            .clickable { menuTipo = true }
                    )
                }
                DropdownMenu(expanded = menuTipo, onDismissRequest = { menuTipo = false }) {
                    Catalogo.TIPOS.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                vm.onTipoServicioChange(tipo)
                                menuTipo = false
                            }
                        )
                    }
                }
            }

            // Fecha con selector de calendario
            CampoValidado(
                value = estado.fechaOperacion,
                onValueChange = vm::onFechaChange,
                label = "Fecha de operación (dd/MM/aaaa)",
                icono = Icons.Default.CalendarMonth,
                error = estado.fechaError,
                enabled = estado.editable,
                trailing = {
                    if (estado.editable) {
                        IconButton(onClick = { mostrarFecha = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Elegir fecha")
                        }
                    }
                }
            )

            CampoValidado(
                value = estado.cliente,
                onValueChange = vm::onClienteChange,
                label = "Cliente (nombre ficticio)",
                icono = Icons.Default.Person,
                error = estado.clienteError,
                enabled = estado.editable
            )

            CampoValidado(
                value = estado.monto,
                onValueChange = vm::onMontoChange,
                label = "Monto ficticio",
                icono = Icons.Default.AttachMoney,
                error = estado.montoError,
                enabled = estado.editable,
                keyboardType = KeyboardType.Decimal
            )

            // Código de autorización + lector QR (recurso nativo: cámara)
            CampoValidado(
                value = estado.codigoAutorizacion,
                onValueChange = vm::onCodigoChange,
                label = "Código de autorización ficticio",
                icono = Icons.Default.Tag,
                error = estado.codigoError,
                enabled = estado.editable,
                trailing = {
                    if (estado.editable) {
                        IconButton(onClick = { navController.navigate("qr") }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Leer código QR")
                        }
                    }
                }
            )

            // Respaldo fotográfico
            Text("Respaldo fotográfico (ficticio)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            RespaldoImage(estado.respaldoUri)
            if (estado.editable) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { navController.navigate("camara") }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Tomar foto")
                    }
                    OutlinedButton(onClick = {
                        galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Galería")
                    }
                }
            }
            AnimatedVisibility(visible = estado.respaldoError != null) {
                Text(estado.respaldoError ?: "", color = MaterialTheme.colorScheme.error)
            }

            CampoValidado(
                value = estado.observaciones,
                onValueChange = vm::onObservacionesChange,
                label = "Observaciones",
                icono = Icons.Default.Notes,
                error = null,
                enabled = estado.editable,
                singleLine = false,
                minLines = 2
            )

            if (estado.editable) {
                val esObservada = estado.estado == EstadoVenta.OBSERVADA
                val puedeEnviar = estado.estado == EstadoVenta.BORRADOR || esObservada
                Button(
                    onClick = { vm.guardar(enviar = false) },
                    enabled = !estado.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (estado.guardando) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Text(if (esNueva || estado.estado == EstadoVenta.BORRADOR) "  Guardar borrador" else "  Guardar cambios")
                }
                if (puedeEnviar) {
                    OutlinedButton(
                        onClick = { vm.guardar(enviar = true) },
                        enabled = !estado.guardando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(if (esObservada) "  Enviar corrección" else "  Enviar a revisión")
                    }
                }
            } else {
                Text(
                    "Esta venta no se puede modificar en su estado actual.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (mostrarFecha) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                        vm.onFechaChange(sdf.format(millis))
                    }
                    mostrarFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { mostrarFecha = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
