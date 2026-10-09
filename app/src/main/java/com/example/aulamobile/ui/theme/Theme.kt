package com.example.aulamobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CineListaDarkColorScheme = darkColorScheme(
    primary = CineOrange,
    onPrimary = CineTextPrimary,
    primaryContainer = CineOrangeDark,
    onPrimaryContainer = CineTextPrimary,
    background = CineBackground,
    onBackground = CineTextPrimary,
    surface = CineSurface,
    onSurface = CineTextPrimary,
    surfaceVariant = CineCardBg,
    onSurfaceVariant = CineTextSecondary,
    outline = CineBorder
)

@Composable
fun CineListaTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CineListaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
