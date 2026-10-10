package de.psdev.devdrawer.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given settings, when shown, then each preference says what it does in plain words`() {
        // Given / When
        composeTestRule.setContent {
            DevDrawerTheme {
                SettingsScreen(
                    viewState = SettingsViewModel.ViewState.Loaded(
                        analyticsVisible = false,
                        settings = SettingsViewModel.Settings(
                            activityChooserEnabled = false,
                            defaultSortOrder = SortOrder.LAST_UPDATED,
                            themeSetting = ThemeSetting.SYSTEM,
                            dynamicColorEnabled = false,
                            analyticsOptIn = false
                        )
                    )
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Ask which screen to open").assertExists()
        composeTestRule.onNodeWithText("When an app has several screens, tapping it in the widget asks which one to start").assertExists()
        composeTestRule.onNodeWithText("Default sort order").assertExists()
        composeTestRule.onNodeWithText("Last updated").assertExists()
        composeTestRule.onNodeWithText("Appearance").assertExists()
    }

    @Test
    fun `given analytics are offered, when shown, then the switch explains what is shared`() {
        // Given / When
        composeTestRule.setContent {
            DevDrawerTheme {
                SettingsScreen(
                    viewState = SettingsViewModel.ViewState.Loaded(
                        analyticsVisible = true,
                        settings = SettingsViewModel.Settings(
                            activityChooserEnabled = false,
                            defaultSortOrder = SortOrder.LAST_UPDATED,
                            themeSetting = ThemeSetting.SYSTEM,
                            dynamicColorEnabled = false,
                            analyticsOptIn = true
                        )
                    )
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Share usage analytics").assertExists()
        composeTestRule.onNodeWithText("Which screens you open and crash reports, via Firebase. No personal data.").assertExists()
    }
}
