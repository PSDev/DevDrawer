package de.psdev.devdrawer.widgets.ui.editor

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class WidgetEditorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given a renamed widget, when editing, then there is nothing to save and removing it is explained`() {
        // Given
        val saved = Widget(id = 1, name = "Work apps", color = 0, profileId = "p1")

        // When
        composeTestRule.setContent {
            DevDrawerTheme {
                WidgetEditor(viewState = WidgetEditorViewState(persistedWidget = saved, editableWidget = saved.copy(name = "Renamed")))
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Save").assertDoesNotExist()
        composeTestRule.onNodeWithText("To remove this widget, long-press it on your home screen and drag it to Remove.").assertExists()
    }
}
