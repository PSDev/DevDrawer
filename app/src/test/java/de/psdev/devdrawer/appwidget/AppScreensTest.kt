package de.psdev.devdrawer.appwidget

import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun activity(name: String, exported: Boolean = true, enabled: Boolean = true, permission: String? = null) =
        ActivityInfo().apply {
            this.name = name
            this.exported = exported
            this.enabled = enabled
            this.permission = permission
        }

    private val labels = mapOf(
        "com.example.MainActivity" to "Example",
        "com.example.settings.SettingsActivity" to "Settings",
        "com.example.DebugActivity" to "Debug menu"
    )

    @Test
    fun `given an app's activities, when listing its screens, then only openable ones remain with the main one first`() {
        // Given
        val activities = listOf(
            activity("com.example.settings.SettingsActivity"),
            activity("com.example.DebugActivity"),
            activity("com.example.MainActivity"),
            activity("com.example.InternalActivity", exported = false),
            activity("com.example.DisabledActivity", enabled = false),
            activity("com.example.ProtectedActivity", permission = "com.example.PRIVATE")
        )

        // When
        val screens = appScreens("com.example", activities, mainClassName = "com.example.MainActivity") { labels.getValue(it.name) }

        // Then
        assertEquals(
            listOf(
                AppScreen("com.example.MainActivity", "Example", ".MainActivity", isMain = true),
                AppScreen("com.example.DebugActivity", "Debug menu", ".DebugActivity", isMain = false),
                AppScreen("com.example.settings.SettingsActivity", "Settings", ".settings.SettingsActivity", isMain = false)
            ),
            screens
        )
    }

    @Test
    fun `given screens, when shown, then the sheet names the app, marks the main screen and opens the tapped one`() {
        // Given
        var opened: AppScreen? = null
        val screens = listOf(
            AppScreen("com.example.MainActivity", "Example", ".MainActivity", isMain = true),
            AppScreen("com.example.DebugActivity", "Debug menu", ".DebugActivity", isMain = false)
        )
        composeTestRule.setContent {
            DevDrawerTheme {
                ChooseScreenSheet(appName = "Example", appIcon = ColorDrawable(0), screens = screens, onScreenSelected = { opened = it }, onDismiss = {})
            }
        }

        // When
        composeTestRule.onNodeWithText("Open Example").assertExists()
        composeTestRule.onNodeWithText("Main screen · .MainActivity").assertExists()
        composeTestRule.onNodeWithText("Debug menu").performClick()

        // Then
        assertEquals(screens[1], opened)
    }
}
