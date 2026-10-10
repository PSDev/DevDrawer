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
        WidgetHeaderColor.RED -> HeaderColorPair(Color(0xFFFFDAD6), Color(0xFF410002))
        WidgetHeaderColor.GREEN -> HeaderColorPair(Color(0xFFB8F397), Color(0xFF042100))
        WidgetHeaderColor.BLUE -> HeaderColorPair(Color(0xFFD1E4FF), Color(0xFF001D36))
        WidgetHeaderColor.TEAL -> HeaderColorPair(Color(0xFF6FF7F6), Color(0xFF002020))
        WidgetHeaderColor.PURPLE -> HeaderColorPair(Color(0xFFEADDFF), Color(0xFF21005D))
        WidgetHeaderColor.DYNAMIC -> null
    }

/** Dark-theme colours of a palette entry; null for [WidgetHeaderColor.DYNAMIC]. */
val WidgetHeaderColor.dark: HeaderColorPair?
    get() = when (this) {
        WidgetHeaderColor.AMBER -> HeaderColorPair(Color(0xFF5E4200), Amber200)
        WidgetHeaderColor.NEUTRAL -> HeaderColorPair(Color(0xFF38342E), Color(0xFFE9E1D9))
        WidgetHeaderColor.DARK -> HeaderColorPair(Color(0xFF000000), Color(0xFFE9E1D9))
        WidgetHeaderColor.RED -> HeaderColorPair(Color(0xFF93000A), Color(0xFFFFDAD6))
        WidgetHeaderColor.GREEN -> HeaderColorPair(Color(0xFF205107), Color(0xFFB8F397))
        WidgetHeaderColor.BLUE -> HeaderColorPair(Color(0xFF00497D), Color(0xFFD1E4FF))
        WidgetHeaderColor.TEAL -> HeaderColorPair(Color(0xFF004F4F), Color(0xFF6FF7F6))
        WidgetHeaderColor.PURPLE -> HeaderColorPair(Color(0xFF4F378B), Color(0xFFEADDFF))
        WidgetHeaderColor.DYNAMIC -> null
    }
