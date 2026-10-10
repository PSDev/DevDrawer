package de.psdev.devdrawer.about

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import de.psdev.devdrawer.BuildConfig
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class AboutScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given a device without the Play Store or a browser, when Play Store is tapped, then it explains instead of crashing`() {
        // Given
        shadowOf(ApplicationProvider.getApplicationContext<android.app.Application>()).checkActivities(true)
        composeTestRule.setContent { DevDrawerTheme { AboutHeader() } }

        // When
        composeTestRule.onNodeWithText("Play Store").performClick()

        // Then
        assertEquals("Your device can't do this", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `given the about header, when shown, then the version line comes from resources`() {
        // Given / When
        composeTestRule.setContent { DevDrawerTheme { AboutHeader() } }

        // Then
        composeTestRule.onNodeWithText("Version ${BuildConfig.VERSION_NAME}").assertExists()
    }
}
