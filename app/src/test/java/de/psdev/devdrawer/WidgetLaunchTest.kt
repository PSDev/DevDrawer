package de.psdev.devdrawer

import android.appwidget.AppWidgetManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetLaunchTest {

    @Test
    fun `given the launcher configures a new widget, when resolving, then setup opens and a result is expected`() {
        // Given / When
        val launch = widgetLaunchFor(action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE, widgetId = 7, openSetup = false)

        // Then
        assertEquals(WidgetLaunch(WidgetSetupRoute(7), isConfiguration = true), launch)
    }

    @Test
    fun `given the widget's empty state, when resolving, then setup opens without a result`() {
        // Given / When
        val launch = widgetLaunchFor(action = null, widgetId = 7, openSetup = true)

        // Then
        assertEquals(WidgetLaunch(WidgetSetupRoute(7), isConfiguration = false), launch)
    }

    @Test
    fun `given the widget's settings button, when resolving, then the editor opens`() {
        // Given / When
        val launch = widgetLaunchFor(action = null, widgetId = 7, openSetup = false)

        // Then
        assertEquals(WidgetLaunch(WidgetEditorRoute(7), isConfiguration = false), launch)
    }

    @Test
    fun `given no widget id, when resolving, then nothing widget-specific opens`() {
        // Given / When
        val launch = widgetLaunchFor(action = null, widgetId = AppWidgetManager.INVALID_APPWIDGET_ID, openSetup = false)

        // Then
        assertNull(launch)
    }
}
