package com.example.gestionventas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorEsquema = lightColorScheme(
    primary = Color(0xFF1F4E8C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFF8A5A00),
    tertiary = Color(0xFF2E7D32),
    error = Color(0xFFB3261E),
    background = Color(0xFFF7F9FC),
    surface = Color.White,
    surfaceVariant = Color(0xFFE3E8F0)
)

@Composable
fun GestionVentasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorEsquema,
        typography = Typography(),
        content = content
    )
}
