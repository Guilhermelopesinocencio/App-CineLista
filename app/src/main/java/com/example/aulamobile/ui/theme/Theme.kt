package com.example.aulamobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema de cores escuro do CineLista, que liga a paleta personalizada (Color.kt)
 * aos "papéis" de cor do Material 3. Os componentes do Material (Button, Card, Text etc.)
 * usam esses papéis automaticamente quando não recebem uma cor explícita.
 */
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

/**
 * Tema principal do aplicativo. Deve envolver todo o conteúdo (normalmente na MainActivity)
 * para que as cores e a tipografia sejam aplicadas em todas as telas.
 */
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