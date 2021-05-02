package de.psdev.devdrawer.widgets

import android.content.Context
import android.graphics.Color
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.receivers.UpdateReceiver
import mu.KLogging

@HiltWorker
class SaveWidgetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val database: DevDrawerDatabase
) : CoroutineWorker(appContext, params) {

    companion object : KLogging() {
        const val ARG_WIDGET_ID = "widgetId"
        const val INVALID_WIDGET_ID = -1
    }

    override suspend fun doWork(): Result {
        val widgetId = inputData.getInt(ARG_WIDGET_ID, INVALID_WIDGET_ID)
        check(widgetId != INVALID_WIDGET_ID) { "Invalid widget ID" }
        val widgetDao = database.widgetDao()
        val widgetProfileDao = database.widgetProfileDao()
        val defaultWidgetProfile = widgetProfileDao.findAll().firstOrNull()
            ?: WidgetProfile(name = "Default").also {
                widgetProfileDao.insert(it)
            }

        // Create entries in database
        val widget = Widget(
            id = widgetId,
            name = "Widget $widgetId",
            color = Color.BLACK,
            profileId = defaultWidgetProfile.id
        )
        widgetDao.insert(widget)
        UpdateReceiver.send(applicationContext)
        return Result.success()
    }
}