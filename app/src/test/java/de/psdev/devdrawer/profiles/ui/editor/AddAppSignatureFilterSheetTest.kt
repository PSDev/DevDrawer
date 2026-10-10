package de.psdev.devdrawer.profiles.ui.editor

import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddAppSignatureFilterSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun app(name: String, packageName: String) = AppInfo(
        name = name,
        packageName = packageName,
        appIcon = ColorDrawable(0),
        firstInstallTime = 0,
        lastUpdateTime = 0,
        signatureHashSha256 = "key-$name"
    )

    private val devDrawer = app("DevDrawer2", "de.psdev.devdrawer")
    private val client = app("Client", "com.example.client")

    private fun showSheet(
        showSystemApps: Boolean = false,
        onShowSystemApps: (Boolean) -> Unit = {},
        onAppSelected: (AppInfo) -> Unit = {}
    ) {
        composeTestRule.setContent {
            DevDrawerTheme {
                AddAppSignatureFilterSheet(
                    viewState = AddAppSignaturePackageFilterDialogViewModel.ViewState.Loaded(
                        data = listOf(devDrawer, client),
                        showSystemApps = showSystemApps
                    ),
                    appSelected = onAppSelected,
                    showSystemApps = onShowSystemApps
                )
            }
        }
    }

    @Test
    fun `given installed apps, when the sheet opens, then it explains the choice and lists the apps`() {
        // Given / When
        showSheet()

        // Then
        composeTestRule.onNodeWithText("Select signature from app").assertExists()
        composeTestRule.onNodeWithText("Every app signed with the same key will match.").assertExists()
        composeTestRule.onNodeWithText("Include system apps").assertExists()
        composeTestRule.onNodeWithText("DevDrawer2").assertExists()
        composeTestRule.onNodeWithText("Client").assertExists()
    }

    @Test
    fun `given a search, when typing part of a package name, then only matching apps remain`() {
        // Given
        showSheet()

        // When
        composeTestRule.onNodeWithText("Search apps").performTextInput("example")

        // Then
        composeTestRule.onNodeWithText("Client").assertExists()
        composeTestRule.onNodeWithText("DevDrawer2").assertDoesNotExist()
    }

    @Test
    fun `given the sheet, when an app is tapped, then it is selected`() {
        // Given
        var selected: AppInfo? = null
        showSheet(onAppSelected = { selected = it })

        // When
        composeTestRule.onNodeWithText("DevDrawer2").performClick()

        // Then
        assertEquals(devDrawer, selected)
    }

    @Test
    fun `given system apps hidden, when the chip is tapped, then system apps are requested`() {
        // Given
        var requested: Boolean? = null
        showSheet(showSystemApps = false, onShowSystemApps = { requested = it })

        // When
        composeTestRule.onNodeWithText("Include system apps").performClick()

        // Then
        assertEquals(true, requested)
    }
}
