package com.example.gestionventas.camera

import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Vista previa CameraX + botón para capturar la foto del respaldo (ficticio).
 * Mismo patrón que QrScanner: PreviewView en AndroidView y la cámara gestionada en DisposableEffect.
 */
@Composable
fun CamaraRespaldo(
    onFotoGuardada: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val callbackFoto = rememberUpdatedState(onFotoGuardada)
    val callbackError = rememberUpdatedState(onError)

    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val imageCapture = remember { ImageCapture.Builder().build() }

    DisposableEffect(context, lifecycleOwner, previewView) {
        val disposed = AtomicBoolean(false)
        val preview = Preview.Builder().build().apply { setSurfaceProvider(previewView.surfaceProvider) }
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var boundProvider: ProcessCameraProvider? = null

        providerFuture.addListener({
            if (!disposed.get()) {
                try {
                    val provider = providerFuture.get()
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                    boundProvider = provider
                } catch (e: Exception) {
                    Log.e("CamaraRespaldo", "Error al iniciar la cámara", e)
                    callbackError.value("No se pudo iniciar la cámara")
                }
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            disposed.set(true)
            boundProvider?.unbind(preview, imageCapture)
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        ExtendedFloatingActionButton(
            onClick = {
                val archivo = ImageStorage.nuevoArchivo(context)
                val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()
                imageCapture.takePicture(
                    opciones,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            callbackFoto.value(Uri.fromFile(archivo).toString())
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CamaraRespaldo", "Error al capturar", exception)
                            callbackError.value("No se pudo capturar la foto")
                        }
                    }
                )
            },
            icon = { Icon(Icons.Default.PhotoCamera, contentDescription = "Capturar") },
            text = { Text("Capturar respaldo") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        )
    }
}
