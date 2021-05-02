package de.psdev.devdrawer.widgets

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.psdev.devdrawer.appwidget.DDWidgetProvider
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import mu.KLogging
import java.util.concurrent.TimeUnit

@HiltWorker
class CleanupWidgetsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val devDrawerDatabase: DevDrawerDatabase
) : CoroutineWorker(context, workerParams) {
    companion object : KLogging() {
        @JvmField
        val TAG: String = CleanupWidgetsWorker::class.java.simpleName

        fun enableWorker(application: Application) {
            val workManager = WorkManager.getInstance(application)

            workManager.enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<CleanupWidgetsWorker>().build()
            )
            workManager.enqueueUniquePeriodicWork(
                TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<CleanupWidgetsWorker>(30, TimeUnit.MINUTES).build()
            )
        }
    }

    override suspend fun doWork(): Result {
        logger.warn { "Cleaning orphaned widgets..." }
        val widgetDao = devDrawerDatabase.widgetDao()
        val widgetManager = AppWidgetManager.getInstance(applicationContext)

        val widgets = widgetDao.findAll()
        val databaseWidgetIds = widgets.map { it.id }
        val appWidgetIds =
            widgetManager.getAppWidgetIds(
                ComponentName(
                    applicationContext,
                    DDWidgetProvider::class.java
                )
            ).toList()

        val deletedWidgets = databaseWidgetIds - appWidgetIds

        if (deletedWidgets.isNotEmpty()) {
            logger.warn { "Deleting orphaned widgets from local database: ${widgets.filter { it.id in deletedWidgets }}" }
            widgetDao.deleteByIds(deletedWidgets)
        }

        val unconfiguredWidgets = appWidgetIds - databaseWidgetIds
        if (unconfiguredWidgets.isNotEmpty()) {
            val widgetProfileDao = devDrawerDatabase.widgetProfileDao()
            val defaultWidgetProfile =
                widgetProfileDao.findAll().firstOrNull() ?: WidgetProfile(name = "Default").also {
                    widgetProfileDao.insert(it)
                }
            for (unconfiguredWidget in unconfiguredWidgets) {
                // Create entries in database
                val widget = Widget(
                    id = unconfiguredWidget,
                    name = "Unconfigured $unconfiguredWidget",
                    color = Color.BLACK,
                    profileId = defaultWidgetProfile.id
                )
                widgetDao.insert(widget)
            }
        }

        return Result.success()
    }
}