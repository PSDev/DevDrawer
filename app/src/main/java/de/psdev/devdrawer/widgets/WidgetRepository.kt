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
) : IWidgetRepository {

    override fun widgetFlow(widgetId: Int) = devDrawerDatabase.widgetDao().widgetWithIdObservable(widgetId)

    override suspend fun update(widget: Widget) {
        devDrawerDatabase.widgetDao().update(widget)
        UpdateReceiver.send(application)
    }

    override suspend fun delete(widget: Widget) {
        devDrawerDatabase.widgetDao().delete(widget)
        UpdateReceiver.send(application)
    }

    override suspend fun findWidgetsForProfile(profileId: String): List<Widget> =
        devDrawerDatabase.widgetDao().findAllByProfileId(profileId)

}