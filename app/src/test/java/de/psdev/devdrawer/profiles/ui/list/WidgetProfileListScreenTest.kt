package de.psdev.devdrawer.profiles.ui.list

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import de.psdev.devdrawer.ui.UiState
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WidgetProfileListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given profiles failed to load, when shown, then it says so instead of spinning`() {
        // Given / When
        composeTestRule.setContent {
            DevDrawerTheme { WidgetProfileListScreen(viewState = UiState.Error(IllegalStateException("database closed"))) }
        }

        // Then
        composeTestRule.onNodeWithText("Couldn't load profiles").assertExists()
        composeTestRule.onNodeWithText("database closed", substring = true).assertExists()
    }
}
