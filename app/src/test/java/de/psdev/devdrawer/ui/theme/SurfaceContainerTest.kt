package de.psdev.devdrawer.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The surface containers Material 3 components draw on must come from DevDrawer's warm
 * neutral palette instead of the Material 3 baseline lilac:
 * cards use surfaceContainerHighest, the NavigationBar surfaceContainer, AlertDialogs surfaceContainerHigh.
 */
class SurfaceContainerTest {

    @Test
    fun `given the light scheme, when reading surfaceContainerHighest, then it is warm neutral tone 90`() {
        // Given
        val scheme = LightColorScheme

        // When
        val result = scheme.surfaceContainerHighest

        // Then
        assertEquals(Color(0xFFE9E1D9), result)
    }

    @Test
    fun `given the dark scheme, when reading surfaceContainerHighest, then it is warm neutral tone 22`() {
        // Given
        val scheme = DarkColorScheme

        // When
        val result = scheme.surfaceContainerHighest

        // Then
        assertEquals(Color(0xFF38342E), result)
    }

    @Test
    fun `given the light scheme, when reading surfaceContainer, then it is warm neutral tone 94`() {
        // Given
        val scheme = LightColorScheme

        // When
        val result = scheme.surfaceContainer

        // Then
        assertEquals(Color(0xFFF5ECE4), result)
    }

    @Test
    fun `given the dark scheme, when reading surfaceContainer, then it is warm neutral tone 12`() {
        // Given
        val scheme = DarkColorScheme

        // When
        val result = scheme.surfaceContainer

        // Then
        assertEquals(Color(0xFF231F1A), result)
    }

    @Test
    fun `given the light scheme, when reading surfaceContainerHigh, then it is warm neutral tone 92`() {
        // Given
        val scheme = LightColorScheme

        // When
        val result = scheme.surfaceContainerHigh

        // Then
        assertEquals(Color(0xFFEFE7DE), result)
    }

    @Test
    fun `given the dark scheme, when reading surfaceContainerHigh, then it is warm neutral tone 17`() {
        // Given
        val scheme = DarkColorScheme

        // When
        val result = scheme.surfaceContainerHigh

        // Then
        assertEquals(Color(0xFF2D2924), result)
    }
}
