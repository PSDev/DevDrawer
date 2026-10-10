package de.psdev.devdrawer.appwidget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.appwidget.glance.DevDrawerGlanceWidget
import de.psdev.devdrawer.database.DevDrawerDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KLogging
import javax.inject.Inject

/**
 * NOTE: Never rename this as it will break existing widgets.
 */
@AndroidEntryPoint
class DDWidgetProvider : GlanceAppWidgetReceiver() {

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    companion object : KLogging()

    override val glanceAppWidget: GlanceAppWidget = DevDrawerGlanceWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        logger.warn { "Deleted widgets ${appWidgetIds.joinToString()}" }
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob()).launch(Dispatchers.IO) {
            try {
                devDrawerDatabase.widgetDao().deleteByIds(appWidgetIds.toList())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
