package com.fingermath.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF1B6EF3),
    surfaceVariant = Color(0xFFD9DEE8),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF7FB0FF),
    surfaceVariant = Color(0xFF3A4150),
)

@Composable
fun FingerMathTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Dark else Light, content = content)
}
