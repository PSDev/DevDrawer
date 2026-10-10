package de.psdev.devdrawer.analytics

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.psdev.devdrawer.SettingsRoute
import de.psdev.devdrawer.WidgetListRoute
import de.psdev.devdrawer.WidgetSetupRoute
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class AnalyticsOptInCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given a pending opt-in, when on the Widgets tab, then the card is shown`() {
        assertTrue(showsAnalyticsOptIn(needsOptIn = true, currentRoute = WidgetListRoute))
    }

    @Test
    fun `given a pending opt-in, when setting up a widget or elsewhere, then nothing interrupts`() {
        assertFalse(showsAnalyticsOptIn(needsOptIn = true, currentRoute = WidgetSetupRoute(5)))
        assertFalse(showsAnalyticsOptIn(needsOptIn = true, currentRoute = SettingsRoute))
    }

    @Test
    fun `given no pending opt-in, when on the Widgets tab, then the card is hidden`() {
        assertFalse(showsAnalyticsOptIn(needsOptIn = false, currentRoute = WidgetListRoute))
    }

    @Test
    fun `given the card, when shown, then it explains the choice and both answers work right away`() {
        // Given
        val answers = mutableListOf<String>()
        composeTestRule.setContent {
            DevDrawerTheme {
                AnalyticsOptInCard(onOptIn = { answers += "in" }, onOptOut = { answers += "out" })
            }
        }

        // When
        composeTestRule.onNodeWithText("Usage analytics").assertExists()
        composeTestRule.onNodeWithText("No personal data", substring = true).assertExists()
        // Crash reports are sent regardless of this choice, so the card must not offer them.
        composeTestRule.onNodeWithText("crash", substring = true, ignoreCase = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("No thanks").assertIsEnabled().performClick()
        composeTestRule.onNodeWithText("Allow").assertIsEnabled().performClick()

        // Then
        assertEquals(listOf("out", "in"), answers)
    }
}
