package de.psdev.devdrawer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.psdev.devdrawer.appwidget.glance.DevDrawerGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KLogging

/** Reloads every placed widget, e.g. after an app was installed or a widget or profile changed. */
class UpdateReceiver: BroadcastReceiver() {
    companion object: KLogging() {

        @JvmStatic
        fun intent(context: Context): Intent = Intent(context, UpdateReceiver::class.java)

        @JvmStatic
        fun send(context: Context) = context.sendBroadcast(intent(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        logger.warn { "onReceive[context=$context, intent=$intent]" }
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob()).launch(Dispatchers.Default) {
            try {
                DevDrawerGlanceWidget.refreshAll(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
