package de.psdev.devdrawer.ui.theme

import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import de.psdev.devdrawer.database.WidgetHeaderColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class WidgetHeaderPaletteTest {

    private val fixedColors = WidgetHeaderColor.entries - WidgetHeaderColor.DYNAMIC

    @Test
    fun `given the palette, when listing it, then it offers the colourful options besides amber, neutral and dark`() {
        // Given / When
        val names = WidgetHeaderColor.entries.map { it.name }

        // Then
        assertEquals(
            listOf("AMBER", "NEUTRAL", "DARK", "RED", "GREEN", "BLUE", "TEAL", "PURPLE", "DYNAMIC"),
            names
        )
    }

    @Test
    fun `given each fixed colour, when reading its light and dark pairs, then text is readable at 4_5 to 1`() {
        // Given / When
        val pairs = fixedColors.flatMap { color -> listOf(color to color.light!!, color to color.dark!!) }

        // Then
        pairs.forEach { (color, pair) ->
            val contrast = ColorUtils.calculateContrast(pair.content.toArgb(), pair.container.toArgb())
            assertTrue("$color contrast $contrast", contrast >= 4.5)
        }
    }

    @Test
    fun `given the fixed colours, when comparing their containers, then each one looks different`() {
        // Given / When
        val light = fixedColors.map { it.light!!.container }
        val dark = fixedColors.map { it.dark!!.container }

        // Then
        assertEquals(fixedColors.size, light.toSet().size)
        assertEquals(fixedColors.size, dark.toSet().size)
    }
}
