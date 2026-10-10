package de.psdev.devdrawer.profiles.ui.editor

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WidgetProfileEditorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val profile = WidgetProfile(id = "p1", name = "Signed like DevDrawer2")

    private fun show(usedBy: List<String>) {
        composeTestRule.setContent {
            DevDrawerTheme {
                WidgetProfileEditor(
                    viewState = WidgetProfileEditorViewState(widgetProfile = profile, widgetName = profile.name, usedByWidgets = usedBy)
                )
            }
        }
    }

    @Test
    fun `given a profile used by widgets, when editing, then it says which widgets the changes affect`() {
        show(listOf("Clients", "Work apps"))

        composeTestRule.onNodeWithText("Used by Clients, Work apps").assertExists()
    }

    @Test
    fun `given a profile no widget uses, when editing, then it says so`() {
        show(emptyList())

        composeTestRule.onNodeWithText("Not used by any widget").assertExists()
    }

    @Test
    fun `given a name update that lags behind, when typing, then the field still shows exactly what was typed`() {
        // Given: the view state never reflects the typed name, like a slow combined flow
        show(emptyList())

        // When
        composeTestRule.onNodeWithText("Signed like DevDrawer2").performTextReplacement("Work apps")

        // Then
        composeTestRule.onNodeWithText("Work apps").assertExists()
    }

    @Test
    fun `given the profile no longer exists, when the editor shows, then it leaves instead of loading forever`() {
        // Given
        var left = false

        // When
        composeTestRule.setContent {
            DevDrawerTheme {
                WidgetProfileEditor(viewState = WidgetProfileEditorViewState(isMissing = true), onMissing = { left = true })
            }
        }

        // Then
        composeTestRule.runOnIdle { assertTrue(left) }
    }
}
