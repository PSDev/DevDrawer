package de.psdev.devdrawer.widgets.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.psdev.devdrawer.database.WidgetHeaderColor
import de.psdev.devdrawer.ui.theme.HeaderColorPair
import de.psdev.devdrawer.ui.theme.dark
import de.psdev.devdrawer.ui.theme.light

/**
 * The colours a widget header gets on the home screen: the palette entry for the system theme, or the
 * wallpaper colours for [WidgetHeaderColor.DYNAMIC] (the app's own primary container before Android 12).
 */
@Composable
fun widgetHeaderColors(headerColor: WidgetHeaderColor): HeaderColorPair {
    val darkTheme = isSystemInDarkTheme()
    val fixed = if (darkTheme) headerColor.dark else headerColor.light
    if (fixed != null) return fixed
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        val scheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        return HeaderColorPair(scheme.primaryContainer, scheme.onPrimaryContainer)
    }
    return HeaderColorPair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
}
