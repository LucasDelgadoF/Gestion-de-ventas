package com.example.gestionventas.ui.venta

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gestionventas.camera.CamaraRespaldo
import com.example.gestionventas.camera.CameraPermissionHelper
import com.example.gestionventas.camera.QrScanner

/** Pide el permiso de cámara (si falta) y luego muestra el contenido. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaConCamara(
    titulo: String,
    navController: NavController,
    contenido: @Composable () -> Unit
) {
    val context = LocalContext.current
    var tienePermiso by remember { mutableStateOf(CameraPermissionHelper.hasCameraPermission(context)) }
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        tienePermiso = concedido
        if (!concedido) {
            Toast.makeText(context, "Se necesita el permiso de cámara", Toast.LENGTH_LONG).show()
        }
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
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding).fillMaxSize()) {
            if (tienePermiso) {
                contenido()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Permiso de cámara requerido", style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Usamos la cámara solo para fotografiar comprobantes ficticios y leer códigos QR de prueba.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { lanzador.launch(Manifest.permission.CAMERA) }) {
                        Text("Conceder permiso de cámara")
                    }
                }
            }
        }
    }
}

/** Captura la foto del respaldo y la deja asociada al formulario de la venta. */
@Composable
fun CapturaRespaldoScreen(navController: NavController, vm: VentaViewModel) {
    val context = LocalContext.current
    PantallaConCamara("Foto de respaldo", navController) {
        CamaraRespaldo(
            onFotoGuardada = { uri ->
                vm.onRespaldoChange(uri)
                Toast.makeText(context, "Respaldo adjuntado", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/** Lee un QR ficticio y lo usa como código de autorización de la venta. */
@Composable
fun EscanerQrScreen(navController: NavController, vm: VentaViewModel) {
    val context = LocalContext.current
    PantallaConCamara("Leer código QR", navController) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.fillMaxWidth().height(400.dp)) {
                QrScanner(
                    onQrCodeScanned = { contenido ->
                        vm.onCodigoChange(contenido.trim())
                        Toast.makeText(context, "QR detectado", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    modifier = Modifier.size(250.dp).align(Alignment.Center),
                    color = Color.Transparent,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {}
            }
            Spacer(Modifier.height(16.dp))
            Text("Enfoque el código QR ficticio en el marco central", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
