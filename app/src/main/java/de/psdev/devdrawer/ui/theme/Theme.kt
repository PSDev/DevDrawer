package de.psdev.devdrawer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Amber200,
    onPrimary = Color(0xFF422D00),
    primaryContainer = Color(0xFF5E4200),
    onPrimaryContainer = Amber200,
    secondary = DeepOrange200,
    onSecondary = Color(0xFF630E00),
    secondaryContainer = Color(0xFF8E1400),
    onSecondaryContainer = DeepOrange200,
    background = Color(0xFF1E1B16),
    onBackground = Color(0xFFE9E1D9),
    surface = Color(0xFF1E1B16),
    onSurface = Color(0xFFE9E1D9),
    surfaceVariant = Color(0xFF4D4639),
    onSurfaceVariant = Color(0xFFD0C5B4),
    outline = Color(0xFF999080)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7B5800),
    onPrimary = Color.White,
    primaryContainer = Amber200,
    onPrimaryContainer = Color(0xFF261900),
    secondary = Color(0xFFB11D00),
    onSecondary = Color.White,
    secondaryContainer = DeepOrange200,
    onSecondaryContainer = Color(0xFF3B0500),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1E1B16),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1E1B16),
    surfaceVariant = Color(0xFFEDE1CF),
    onSurfaceVariant = Color(0xFF4D4639),
    outline = Color(0xFF7F7667)
)

@Composable
fun DevDrawerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
