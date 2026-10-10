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

enum class SetupExit { FINISH_ACTIVITY, GO_BACK }

/**
 * How leaving setup (done or back) continues: setup opened from outside the app (launcher or widget) closes the
 * activity and returns to the home screen; setup opened inside the app goes back.
 */
fun setupExitFor(widgetId: Int, externalSetupWidgetId: Int): SetupExit =
    if (widgetId == externalSetupWidgetId) SetupExit.FINISH_ACTIVITY else SetupExit.GO_BACK
