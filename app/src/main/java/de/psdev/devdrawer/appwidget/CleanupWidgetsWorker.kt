package de.psdev.devdrawer.appwidget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.psdev.devdrawer.database.DevDrawerDatabase
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
                PeriodicWorkRequestBuilder<CleanupWidgetsWorker>(
                    WorkerConstants.PERIODIC_INTERVAL_MINUTES,
                    TimeUnit.MINUTES
                ).build()
            )
        }
    }

    override suspend fun doWork(): Result {
        logger.warn { "Cleaning orphaned widgets..." }
        val widgetDao = devDrawerDatabase.widgetDao()
        val widgetManager = AppWidgetManager.getInstance(applicationContext)

        val widgets = widgetDao.findAll()
        val databaseWidgetIds = widgets.map { it.id }
        val appWidgetIds = widgetManager.getAppWidgetIds(
            ComponentName(
                applicationContext,
                DDWidgetProvider::class.java
            )
        )

        val deletedWidgets = databaseWidgetIds - appWidgetIds.toSet()

        if (deletedWidgets.isNotEmpty()) {
            logger.warn { "Deleting orphaned widgets from local database: $deletedWidgets" }
            widgetDao.deleteByIds(deletedWidgets)
        }
        return Result.success()
    }
}
