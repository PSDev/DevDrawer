package de.psdev.devdrawer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = LightGreen500,
    primaryVariant = LightGreen200,
    secondary = Orange700,
    background = Color(0xFF575757),
    surface = Color(0xFF575757),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

private val LightColorPalette = lightColors(
    primary = LightGreen500,
    primaryVariant = LightGreen200,
    secondary = Orange700,
    surface = Color(0xFFDDDDDD)
)

@Composable
fun DevDrawerTheme(
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
//        typography = Typography,
//        shapes = Shapes,
        content = content
    )
}