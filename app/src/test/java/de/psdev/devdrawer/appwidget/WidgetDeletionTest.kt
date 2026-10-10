package de.psdev.devdrawer.appwidget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class WidgetDeletionTest {

    private val application: Application = ApplicationProvider.getApplicationContext()
    private val workManager get() = WorkManager.getInstance(application)

    @Before
    fun setUp() {
        // Stands in for the Hilt worker factory: every worker just succeeds, so its state shows it ran.
        val factory = object : WorkerFactory() {
            override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters) =
                object : Worker(appContext, workerParameters) {
                    override fun doWork() = Result.success()
                }
        }
        WorkManagerTestInitHelper.initializeTestWorkManager(
            application,
            Configuration.Builder().setWorkerFactory(factory).setExecutor(SynchronousExecutor()).build()
        )
    }

    private fun cleanupStates() =
        workManager.getWorkInfosForUniqueWork(CleanupWidgetsWorker.CLEANUP_NOW).get().map { it.state }

    @Test
    fun `given a widget removed from the home screen, when the provider is told, then it does not crash and runs a cleanup`() {
        // Given
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_DELETED)
            .setComponent(ComponentName(application, DDWidgetProvider::class.java))
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 7)

        // When
        application.sendBroadcast(intent)
        shadowOf(application.mainLooper).idle()

        // Then
        assertEquals(listOf(WorkInfo.State.SUCCEEDED), cleanupStates())
    }

    @Test
    fun `given the app starts, when the cleanup workers are enabled, then the one-off cleanup runs and is not cancelled by the periodic one`() {
        // Given / When
        CleanupWidgetsWorker.enableWorker(application)

        // Then
        assertEquals(listOf(WorkInfo.State.SUCCEEDED), cleanupStates())
        assertEquals(
            listOf(WorkInfo.State.ENQUEUED),
            workManager.getWorkInfosForUniqueWork(CleanupWidgetsWorker.TAG).get().map { it.state }
        )
    }
}
