package de.psdev.devdrawer.profiles.ui.editor

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Dialog buttons use the string resources' sentence case ("Cancel"), not upper-cased labels ("CANCEL").
 */
@RunWith(RobolectricTestRunner::class)
class DialogButtonLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val packageFilter = PackageFilter(
        type = FilterType.SIGNATURE,
        filter = "4f8d2c9e",
        description = "Example App",
        profileId = "profile"
    )

    @Test
    fun `given the package pattern sheet, when shown, then its button is in sentence case`() {
        // Given / When
        composeTestRule.setContent {
            DevDrawerTheme {
                PackagePatternSheet(
                    preview = PatternPreview(pattern = "com.example.*", isValid = true),
                    onPatternChange = {},
                    onAdd = {},
                    onDismiss = {}
                )
            }
        }

        // Then
        assertSentenceCase("Add")
    }

    @Test
    fun `given the filter info dialog, when shown, then its button is in sentence case`() {
        // Given / When
        composeTestRule.setContent {
            DevDrawerTheme { PackageFilterInfoDialog(packageFilter = packageFilter, onDismiss = {}) }
        }

        // Then
        assertSentenceCase("Close")
    }

    private fun assertSentenceCase(vararg labels: String) {
        labels.forEach { label ->
            composeTestRule.onNodeWithText(label).assertExists()
            composeTestRule.onNodeWithText(label.uppercase()).assertDoesNotExist()
        }
    }
}
