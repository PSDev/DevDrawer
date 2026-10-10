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
import org.robolectric.annotation.Config

/**
 * Dialog buttons use the string resources' sentence case ("Cancel"), not upper-cased labels ("CANCEL").
 */
@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
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
    fun `given the add package name filter dialog, when loaded, then its buttons are in sentence case`() {
        // Given / When
        composeTestRule.setContent {
            DevDrawerTheme {
                AddPackageNamePackageFilterDialog(
                    viewState = AddPackageNamePackageFilterDialogViewModel.ViewState.Loaded(listOf("com.example.app"))
                )
            }
        }

        // Then
        assertSentenceCase("Cancel", "Add")
    }

    @Test
    fun `given the add app signature filter dialog, when shown, then its button is in sentence case`() {
        // Given / When
        composeTestRule.setContent {
            DevDrawerTheme {
                AddAppSignaturePackageFilterDialog(
                    viewState = AddAppSignaturePackageFilterDialogViewModel.ViewState.Loaded(
                        data = emptyList(),
                        showSystemApps = false
                    )
                )
            }
        }

        // Then
        assertSentenceCase("Cancel")
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
