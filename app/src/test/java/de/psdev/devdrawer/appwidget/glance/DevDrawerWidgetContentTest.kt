package de.psdev.devdrawer.appwidget.glance

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasContentDescription
import androidx.glance.testing.unit.hasText
import androidx.test.core.app.ApplicationProvider
import de.psdev.devdrawer.database.WidgetHeaderColor
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class DevDrawerWidgetContentTest {

    private fun state(apps: List<WidgetAppItem>) = WidgetUiState(
        appWidgetId = 5,
        title = "Work apps",
        headerColor = WidgetHeaderColor.AMBER,
        updatedAt = "15:12",
        apps = apps
    )

    @Test
    fun `given a widget with no apps, when rendered, then it explains the empty list and offers setup`() =
        runGlanceAppWidgetUnitTest {
            // Given
            setContext(ApplicationProvider.getApplicationContext())
            setAppWidgetSize(DpSize(300.dp, 300.dp))

            // When
            provideComposable { DevDrawerWidgetContent(state(apps = emptyList())) }

            // Then
            onNode(hasText("Work apps")).assertExists()
            onNode(hasText("No apps · updated 15:12")).assertExists()
            onNode(hasText("No apps match this widget")).assertExists()
            onNode(hasText("Choose apps")).assertExists()
        }

    @Test
    fun `given a widget with apps, when rendered, then each app shows its name, package and actions`() =
        runGlanceAppWidgetUnitTest {
            // Given
            setContext(ApplicationProvider.getApplicationContext())
            setAppWidgetSize(DpSize(300.dp, 300.dp))
            val apps = listOf(
                WidgetAppItem(name = "DevDrawer2", packageName = "de.psdev.devdrawer", icon = null),
                WidgetAppItem(name = "Client", packageName = "com.example.client", icon = null)
            )

            // When
            provideComposable { DevDrawerWidgetContent(state(apps)) }

            // Then
            onNode(hasText("2 apps · updated 15:12")).assertExists()
            onNode(hasText("DevDrawer2")).assertExists()
            onNode(hasText("de.psdev.devdrawer")).assertExists()
            onNode(hasContentDescription("Uninstall DevDrawer2")).assertExists()
            onNode(hasContentDescription("App details for DevDrawer2")).assertExists()
            onNode(hasText("No apps match this widget")).assertDoesNotExist()
        }
}
