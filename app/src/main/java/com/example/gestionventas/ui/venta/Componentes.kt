package com.example.gestionventas.ui.venta

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gestionventas.camera.ImageStorage
import com.example.gestionventas.model.EstadoVenta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Campo de texto estándar de la app: ícono, error visual (borde rojo + ícono + mensaje animado). */
@Composable
fun CampoValidado(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    trailing: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            if (error != null) {
                Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
            } else {
                trailing?.invoke()
            }
        },
        isError = error != null,
        supportingText = {
            AnimatedVisibility(visible = error != null) {
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
            }
        },
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}

fun colorEstado(estado: String): Color = when (estado) {
    EstadoVenta.BORRADOR -> Color(0xFF757575)
    EstadoVenta.PENDIENTE -> Color(0xFFEF6C00)
    EstadoVenta.OBSERVADA -> Color(0xFFC62828)
    EstadoVenta.CORREGIDA -> Color(0xFF6A1B9A)
    EstadoVenta.VALIDADA -> Color(0xFF2E7D32)
    EstadoVenta.RECHAZADA -> Color(0xFF424242)
    else -> Color(0xFF1565C0)
}

/** Etiqueta de estado; el color cambia con animación cuando la venta cambia de estado. */
@Composable
fun EstadoChip(estado: String, modifier: Modifier = Modifier) {
    val color by animateColorAsState(targetValue = colorEstado(estado), label = "colorEstado")
    Surface(color = color, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = estado,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Muestra la imagen del respaldo (archivo interno), decodificada fuera del hilo principal. */
@Composable
fun RespaldoImage(uri: String?, modifier: Modifier = Modifier) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = uri) {
        value = if (uri == null) null else withContext(Dispatchers.IO) { ImageStorage.cargarBitmap(uri) }
    }
    val img = bitmap
    if (img != null) {
        Image(
            bitmap = img.asImageBitmap(),
            contentDescription = "Respaldo fotográfico ficticio",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    } else {
        Text(
            text = if (uri == null) "Sin respaldo adjunto" else "No se pudo cargar la imagen",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
    }
}

fun formatoMonto(m: Double): String = "$" + String.format(Locale("es", "CL"), "%,.0f", m)

fun formatoFechaHora(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date(millis))
