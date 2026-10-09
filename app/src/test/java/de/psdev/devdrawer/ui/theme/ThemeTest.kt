package de.psdev.devdrawer.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTest {

    @Test
    fun `given the light scheme, when reading inversePrimary, then it is the dark scheme's primary`() {
        // Given
        val scheme = LightColorScheme

        // When
        val result = scheme.inversePrimary

        // Then
        assertEquals(DarkColorScheme.primary, result)
    }

    @Test
    fun `given the dark scheme, when reading inversePrimary, then it is the light scheme's primary`() {
        // Given
        val scheme = DarkColorScheme

        // When
        val result = scheme.inversePrimary

        // Then
        assertEquals(LightColorScheme.primary, result)
    }
}
