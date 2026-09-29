package com.evecus.gitssh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = darkColorScheme(
    primary = Color(0xFF7CDBB0),
    secondary = Color(0xFF8AB4F8),
    background = Color(0xFF0F1115),
    surface = Color(0xFF171A21),
    error = Color(0xFFFF8A80),
)

@Composable
fun GitSshTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
