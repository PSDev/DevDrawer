package de.psdev.devdrawer

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import de.psdev.devdrawer.appwidget.UpdateWidgetsWorker
import de.psdev.devdrawer.receivers.AppInstallationReceiver
import de.psdev.devdrawer.widgets.CleanupWidgetsWorker
import mu.KLogging
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltAndroidApp
class DevDrawerApplication: Application(), Configuration.Provider {

    companion object: KLogging();

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val appInstallationReceiver: AppInstallationReceiver = AppInstallationReceiver()

    override fun onCreate() {
        measureTimeMillis {
            super.onCreate()
            registerAppInstallationReceiver()
            setupWorkers()
        }.let {
            logger.warn(
                "{} version {} ({}) took {}ms to init",
                this::class.java.simpleName,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                it
            )
        }
    }

    // ==========================================================================================================================
    // Configuration.Provider
    // ==========================================================================================================================

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private fun registerAppInstallationReceiver() {
        registerReceiver(appInstallationReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        })
    }

    private fun setupWorkers() {
        UpdateWidgetsWorker.enableWorker(this)
        CleanupWidgetsWorker.enableWorker(this)
    }

}