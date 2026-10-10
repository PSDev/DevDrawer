package de.psdev.devdrawer.ui.theme

import androidx.compose.ui.graphics.Color
import de.psdev.devdrawer.database.WidgetHeaderColor

/** A header colour pair: the container and the text and icons on it, each readable at 4.5:1 or better. */
data class HeaderColorPair(val container: Color, val content: Color)

/** Light-theme colours of a palette entry; null for [WidgetHeaderColor.DYNAMIC], which comes from the wallpaper. */
val WidgetHeaderColor.light: HeaderColorPair?
    get() = when (this) {
        WidgetHeaderColor.AMBER -> HeaderColorPair(Amber200, Color(0xFF261900))
        WidgetHeaderColor.NEUTRAL -> HeaderColorPair(Color(0xFFE9E1D9), Color(0xFF1E1B16))
        WidgetHeaderColor.DARK -> HeaderColorPair(Color(0xFF1E1B16), Color(0xFFFFFFFF))
        WidgetHeaderColor.DYNAMIC -> null
    }

/** Dark-theme colours of a palette entry; null for [WidgetHeaderColor.DYNAMIC]. */
val WidgetHeaderColor.dark: HeaderColorPair?
    get() = when (this) {
        WidgetHeaderColor.AMBER -> HeaderColorPair(Color(0xFF5E4200), Amber200)
        WidgetHeaderColor.NEUTRAL -> HeaderColorPair(Color(0xFF38342E), Color(0xFFE9E1D9))
        WidgetHeaderColor.DARK -> HeaderColorPair(Color(0xFF000000), Color(0xFFE9E1D9))
        WidgetHeaderColor.DYNAMIC -> null
    }
