package de.psdev.devdrawer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class ShapeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given the app theme, when reading its shapes, then they are the Material 3 corner scale`() {
        // Given
        var shapes: Shapes? = null

        // When
        composeTestRule.setContent {
            DevDrawerTheme { shapes = MaterialTheme.shapes }
        }

        // Then: FAB 16dp, cards 12dp, small surfaces 8dp instead of the old 0/4/4dp
        assertEquals(Shapes(), shapes)
    }
}
