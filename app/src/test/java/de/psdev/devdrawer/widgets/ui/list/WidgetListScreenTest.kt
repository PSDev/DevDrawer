package de.psdev.devdrawer.widgets.ui.list

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class WidgetListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val hint = "Long-press your home screen, tap Widgets and drag DevDrawer onto it."

    @Test
    fun `given no widgets and a launcher that can't add them from the app, when shown, then it explains how to add one`() {
        // Given / When
        composeTestRule.setContent {
            DevDrawerTheme { WidgetListScreen(WidgetListScreenState.Loaded(emptyList(), isRequestPinAppWidgetSupported = false)) }
        }

        // Then
        composeTestRule.onNodeWithText("No widgets created").assertExists()
        composeTestRule.onNodeWithText(hint).assertExists()
    }

    @Test
    fun `given no widgets and a launcher that can add them, when shown, then the button is announced once`() {
        // Given / When
        composeTestRule.setContent {
            DevDrawerTheme { WidgetListScreen(WidgetListScreenState.Loaded(emptyList(), isRequestPinAppWidgetSupported = true)) }
        }

        // Then
        composeTestRule.onNodeWithText("Add widget").assertExists()
        composeTestRule.onAllNodesWithContentDescription("Add widget").assertCountEquals(0)
        composeTestRule.onNodeWithText(hint).assertDoesNotExist()
    }
}
