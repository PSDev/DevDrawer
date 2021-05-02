package de.psdev.devdrawer.receivers

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.psdev.devdrawer.widgets.SaveWidgetWorker
import mu.KLogging

class PinWidgetSuccessReceiver : BroadcastReceiver() {

    companion object : KLogging() {
        fun intent(context: Context): Intent = Intent(context, PinWidgetSuccessReceiver::class.java)
    }

    override fun onReceive(context: Context, intent: Intent) {
        logger.warn { "onReceive[context=$context, intent=$intent]" }
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val inputData = Data.Builder().putInt(SaveWidgetWorker.ARG_WIDGET_ID, widgetId).build()
            val request = OneTimeWorkRequestBuilder<SaveWidgetWorker>()
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "SAVE_WIDGET_$widgetId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}