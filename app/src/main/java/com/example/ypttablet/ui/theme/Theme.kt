package com.example.ypttablet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val YptDarkBg = Color(0xFF121615)
val YptCardBg = Color(0xFF1C2422)
val YptPrimaryGreen = Color(0xFF00C896)
val YptAccentOrange = Color(0xFFFF6D3F)
val YptTextPrimary = Color(0xFFF1F5F4)
val YptTextSecondary = Color(0xFF94A39E)

private val YptColorScheme = darkColorScheme(
    primary = YptPrimaryGreen,
    secondary = YptAccentOrange,
    background = YptDarkBg,
    surface = YptCardBg,
    onPrimary = Color.Black,
    onBackground = YptTextPrimary,
    onSurface = YptTextPrimary
)

@Composable
fun YptTabletTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YptColorScheme,
        content = content
    )
}
