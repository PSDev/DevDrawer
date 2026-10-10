package de.psdev.devdrawer.appwidget.glance

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/** The header's reload button: redraws this widget with the currently installed apps. */
class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        DevDrawerGlanceWidget.refresh(context, glanceId)
    }
}
