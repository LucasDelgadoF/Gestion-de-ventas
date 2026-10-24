package com.example.gestionventas.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/**
 * Almacenamiento de los respaldos fotográficos (FICTICIOS) en el almacenamiento interno de la app.
 * Restringe tipo y tamaño de archivo y permite eliminar los archivos de prueba.
 */
object ImageStorage {
    const val MAX_BYTES = 5L * 1024 * 1024
    private val TIPOS_PERMITIDOS = listOf("image/jpeg", "image/png")

    fun carpeta(context: Context): File =
        File(context.filesDir, "respaldos").apply { if (!exists()) mkdirs() }

    fun nuevoArchivo(context: Context): File =
        File(carpeta(context), "respaldo_${System.currentTimeMillis()}.jpg")

    /** Copia una imagen elegida de la galería validando tipo y tamaño. Devuelve (uri, error). */
    fun importar(context: Context, origen: Uri): Pair<String?, String?> {
        val tipo = context.contentResolver.getType(origen)
        if (tipo == null || tipo !in TIPOS_PERMITIDOS) return null to "Solo se permiten imágenes JPG o PNG"
        return try {
            val destino = nuevoArchivo(context)
            context.contentResolver.openInputStream(origen)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            }
            if (destino.length() > MAX_BYTES) {
                destino.delete()
                null to "La imagen supera los 5 MB permitidos"
            } else {
                Uri.fromFile(destino).toString() to null
            }
        } catch (e: Exception) {
            null to "No se pudo leer la imagen"
        }
    }

    /** Decodifica reducida para mostrar sin consumir memoria de más. */
    fun cargarBitmap(uriString: String, maxLado: Int = 800): Bitmap? {
        val ruta = Uri.parse(uriString).path ?: return null
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, opts)
        var sample = 1
        while (opts.outWidth / sample > maxLado || opts.outHeight / sample > maxLado) sample *= 2
        return BitmapFactory.decodeFile(ruta, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    fun eliminarTodos(context: Context) {
        carpeta(context).listFiles()?.forEach { it.delete() }
    }
}
