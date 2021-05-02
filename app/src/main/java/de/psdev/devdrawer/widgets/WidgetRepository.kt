package de.psdev.devdrawer.widgets

import android.app.Application
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.receivers.UpdateReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepository @Inject constructor(
    private val application: Application,
    private val devDrawerDatabase: DevDrawerDatabase
) {

    fun widgetFlow(widgetId: Int) = devDrawerDatabase.widgetDao().widgetWithIdObservable(widgetId)

    suspend fun update(widget: Widget) {
        devDrawerDatabase.widgetDao().update(widget)
        UpdateReceiver.send(application)
    }

}