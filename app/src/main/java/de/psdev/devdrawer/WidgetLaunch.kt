package de.psdev.devdrawer

import android.appwidget.AppWidgetManager
import androidx.navigation3.runtime.NavKey

/**
 * Where an intent about a home-screen widget leads. [isConfiguration] is true when the launcher placed or
 * reconfigures the widget and waits for this activity's result.
 */
data class WidgetLaunch(val route: NavKey, val isConfiguration: Boolean)

fun widgetLaunchFor(action: String?, widgetId: Int, openSetup: Boolean): WidgetLaunch? = when {
    widgetId == AppWidgetManager.INVALID_APPWIDGET_ID -> null
    action == AppWidgetManager.ACTION_APPWIDGET_CONFIGURE -> WidgetLaunch(WidgetSetupRoute(widgetId), isConfiguration = true)
    openSetup -> WidgetLaunch(WidgetSetupRoute(widgetId), isConfiguration = false)
    else -> WidgetLaunch(WidgetEditorRoute(widgetId), isConfiguration = false)
}
