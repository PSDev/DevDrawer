package de.psdev.devdrawer

import android.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.profiles.DeleteDialogState
import de.psdev.devdrawer.profiles.WidgetInUseErrorAlertDialog
import de.psdev.devdrawer.profiles.ui.editor.AddAppSignaturePackageFilterDialog
import de.psdev.devdrawer.profiles.ui.editor.AddAppSignaturePackageFilterDialogViewModel
import de.psdev.devdrawer.profiles.ui.editor.AddPackageNamePackageFilterDialog
import de.psdev.devdrawer.profiles.ui.editor.AddPackageNamePackageFilterDialogViewModel
import de.psdev.devdrawer.profiles.ui.editor.PackageFilterPreviewDialog
import de.psdev.devdrawer.profiles.ui.editor.PackageFilterPreviewDialogViewModel
import de.psdev.devdrawer.settings.ListPreference
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Dialog text comes from string resources, so it is translated: under a German locale every dialog reads German.
 */
@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33], qualifiers = "de")
class DialogStringResourcesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val widgetProfile = WidgetProfile(name = "Arbeit")

    @Test
    fun `given a German locale, when showing the analytics opt-in dialog, then it reads German`() {
        // Given / When
        composeTestRule.setContent {
            DevDrawerTheme { AnalyticsOptInDialog(onOptIn = {}, onOptOut = {}) }
        }

        // Then
        assertTexts("Nutzungsanalyse", "Zustimmen", "Ablehnen")
        composeTestRule.onNodeWithText("Wir verwenden Firebase Analytics", substring = true).assertExists()
    }

    @Test
    fun `given a German locale, when showing the profile in use error, then it reads German`() {
        // Given
        val state = DeleteDialogState.InUseError(
            widgetProfile = widgetProfile,
            widgets = listOf(Widget(1, "Widget 1", Color.BLACK, widgetProfile.id))
        )

        // When
        composeTestRule.setContent {
            DevDrawerTheme { WidgetInUseErrorAlertDialog(state = state) }
        }

        // Then
        assertTexts("Profil kann nicht gelöscht werden, da es noch von Widgets verwendet wird", "Schließen")
        composeTestRule.onNodeWithText("Das Profil Arbeit wird verwendet von:", substring = true).assertExists()
    }

    @Test
    fun `given a German locale, when opening a list preference, then the default dialog title reads German`() {
        // Given
        composeTestRule.setContent {
            DevDrawerTheme {
                ListPreference(
                    label = "Sortierung",
                    values = mapOf(SortOrder.LAST_UPDATED to "Zuletzt aktualisiert"),
                    currentValue = SortOrder.LAST_UPDATED
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Sortierung").performClick()

        // Then
        assertTexts("Option auswählen")
    }

    @Test
    fun `given a German locale, when a filter dialog fails to load, then the error reads German`() {
        // Given / When
        composeTestRule.setContent {
            DevDrawerTheme {
                AddAppSignaturePackageFilterDialog(
                    viewState = AddAppSignaturePackageFilterDialogViewModel.ViewState.Error("kaputt")
                )
                AddPackageNamePackageFilterDialog(
                    viewState = AddPackageNamePackageFilterDialogViewModel.ViewState.Error("defekt")
                )
                PackageFilterPreviewDialog(
                    viewState = PackageFilterPreviewDialogViewModel.ViewState.Error("weg")
                )
            }
        }

        // Then
        assertTexts("Fehler: kaputt", "Fehler: defekt", "Fehler: weg")
    }

    private fun assertTexts(vararg texts: String) {
        texts.forEach { composeTestRule.onNodeWithText(it).assertExists() }
    }
}
