package com.github.jetbrains.rssreader.androidApp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 🎨 Colores personalizados según el mockup
val AzulOscuroFondo = Color(0xFF1E2A36)
val AzulTextoClaro = Color(0xFFBFD9E2)
val RosaBoton = Color(0xFFF26A8D)
val GrisClaro = Color(0xFFF5F5F5)

val VerdeFluor = Color(0xFF00FF41) // Si aún lo necesitas para otro uso

private val DarkColorPalette = darkColors(
    primary = RosaBoton, // botón principal
    primaryVariant = RosaBoton,
    secondary = AzulTextoClaro,
    background = AzulOscuroFondo,
    surface = AzulOscuroFondo,
    onPrimary = Color.White,     // texto sobre botón
    onSecondary = Color.White,
    onBackground = AzulTextoClaro,
    onSurface = AzulTextoClaro,
)

private val LightColorPalette = lightColors(
    primary = RosaBoton,
    primaryVariant = RosaBoton,
    secondary = AzulTextoClaro,
    background = AzulOscuroFondo,
    surface = AzulOscuroFondo,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = AzulTextoClaro,
    onSurface = AzulTextoClaro,
)

@Composable
fun PeluqueriaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}