package de.psdev.devdrawer.receivers

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import mu.KLogging

/** Called by the launcher once the user confirms placing a widget requested from inside the app. */
class PinWidgetSuccessReceiver : BroadcastReceiver() {

    companion object : KLogging() {
        fun intent(context: Context): Intent = Intent(context, PinWidgetSuccessReceiver::class.java)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        logger.info { "Widget $widgetId pinned" }
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            PinnedWidgets.widgetPinned(widgetId)
        }
    }
}
