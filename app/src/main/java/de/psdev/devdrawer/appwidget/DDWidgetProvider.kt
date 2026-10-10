package de.psdev.devdrawer.appwidget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import de.psdev.devdrawer.appwidget.glance.DevDrawerGlanceWidget
import mu.KLogging

/**
 * NOTE: Never rename this as it will break existing widgets.
 */
class DDWidgetProvider : GlanceAppWidgetReceiver() {

    companion object : KLogging()

    override val glanceAppWidget: GlanceAppWidget = DevDrawerGlanceWidget()

    /**
     * Glance's own [onDeleted] already takes the broadcast's goAsync() to clear the widget's state; a second
     * goAsync() returns null. The database records are removed by a worker instead.
     */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        logger.info { "Deleted widgets ${appWidgetIds.joinToString()}" }
        CleanupWidgetsWorker.runOnce(context)
    }
}
