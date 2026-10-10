package de.psdev.devdrawer.profiles.ui.editor

import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class PackagePatternSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun app(name: String, packageName: String) = AppInfo(
        name = name,
        packageName = packageName,
        appIcon = ColorDrawable(0),
        firstInstallTime = 0,
        lastUpdateTime = 0,
        signatureHashSha256 = "key"
    )

    private fun show(preview: PatternPreview, onPatternChange: (String) -> Unit = {}, onAdd: (String) -> Unit = {}) {
        composeTestRule.setContent {
            DevDrawerTheme {
                PackagePatternSheet(preview = preview, onPatternChange = onPatternChange, onAdd = onAdd, onDismiss = {})
            }
        }
    }

    @Test
    fun `given a pattern matching apps, when shown, then it counts and lists them and can be added`() {
        // Given
        var added: String? = null
        val preview = PatternPreview(
            pattern = "com.example.*",
            isValid = true,
            matchCount = 2,
            apps = listOf(app("Client", "com.example.client"), app("Other", "com.example.other"))
        )

        // When
        show(preview, onAdd = { added = it })

        // Then
        composeTestRule.onNodeWithText("2 apps match").assertExists()
        composeTestRule.onNodeWithText("Client").assertExists()
        composeTestRule.onNodeWithText("com.example.other", substring = true).assertExists()
        composeTestRule.onNodeWithText("Add").assertIsEnabled().performClick()
        assertEquals("com.example.*", added)
    }

    @Test
    fun `given an invalid pattern, when shown, then it says so and cannot be added`() {
        // Given / When
        show(PatternPreview(pattern = "com.(", isValid = false, matchCount = 0, apps = emptyList()))

        // Then
        composeTestRule.onNodeWithText("Not a valid pattern").assertExists()
        composeTestRule.onNodeWithText("Add").assertIsNotEnabled()
    }

    @Test
    fun `given the sheet, when typing, then the pattern is reported`() {
        // Given
        val typed = mutableListOf<String>()
        show(PatternPreview(), onPatternChange = { typed += it })

        // When
        composeTestRule.onNodeWithText("Package filter").performTextInput("com.")

        // Then
        assertEquals("com.", typed.last())
    }

    @Test
    fun `given a preview that lags behind, when typing, then the field still shows exactly what was typed`() {
        // Given: the preview never catches up, like a slow recomputation
        show(PatternPreview())

        // When
        composeTestRule.onNodeWithText("Package filter").performTextInput("com.example.*")

        // Then
        composeTestRule.onNodeWithText("com.example.*").assertExists()
    }

    @Test
    fun `given a pattern that matches no app yet, when shown, then it cannot be added`() {
        // Given / When: typing has only just started
        show(PatternPreview(pattern = "c", isValid = true, matchCount = 0, apps = emptyList()))

        // Then
        composeTestRule.onNodeWithText("0 apps match").assertExists()
        composeTestRule.onNodeWithText("Add").assertIsNotEnabled()
    }
}
