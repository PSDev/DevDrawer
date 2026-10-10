package de.psdev.devdrawer.profiles.ui.editor

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The filter type labels ("Package name", "App signature") are shown in sentence case,
 * neither upper-cased ("PACKAGE NAME") nor lower-cased ("package name").
 */
@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class FilterTypeLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given the profile editor with filters, when loaded, then filters read as plain sentences`() {
        // Given
        val widgetProfile = WidgetProfile(id = "p1", name = "Test profile")
        val filters = listOf(
            PackageFilter(id = "a", type = FilterType.SIGNATURE, filter = "key", description = "DevDrawer2", profileId = "p1"),
            PackageFilter(id = "b", type = FilterType.PACKAGE_NAME, filter = "com.example.*", profileId = "p1")
        )

        // When
        composeTestRule.setContent {
            DevDrawerTheme {
                WidgetProfileEditor(
                    viewState = WidgetProfileEditorViewState(
                        widgetProfile = widgetProfile,
                        widgetName = widgetProfile.name,
                        packageFilters = filters,
                        filterAppCounts = mapOf("a" to 2, "b" to 1)
                    )
                )
            }
        }

        // Then
        assertSentenceCase("Signed like DevDrawer2", "Package matches com.example.*", "Add filter")
        composeTestRule.onNodeWithText("Same signing key · 2 apps").assertExists()
        composeTestRule.onNodeWithText("Package name pattern · 1 app").assertExists()
    }

    @Test
    fun `given a package name filter, when showing its info, then the title is in sentence case`() {
        // Given
        val packageFilter = PackageFilter(
            type = FilterType.PACKAGE_NAME,
            filter = "com.example.*",
            profileId = "profile"
        )

        // When
        composeTestRule.setContent {
            DevDrawerTheme { PackageFilterInfoDialog(packageFilter = packageFilter, onDismiss = {}) }
        }

        // Then
        assertSentenceCase("Package name")
    }

    private fun assertSentenceCase(vararg labels: String) {
        labels.forEach { label ->
            composeTestRule.onNodeWithText(label).assertExists()
            composeTestRule.onNodeWithText(label.uppercase()).assertDoesNotExist()
            composeTestRule.onNodeWithText(label.lowercase()).assertDoesNotExist()
        }
    }
}
