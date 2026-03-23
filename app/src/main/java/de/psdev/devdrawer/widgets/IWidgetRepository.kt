package de.psdev.devdrawer.widgets

import de.psdev.devdrawer.database.Widget
import kotlinx.coroutines.flow.Flow

interface IWidgetRepository {
    fun widgetFlow(widgetId: Int): Flow<Widget?>
    suspend fun update(widget: Widget)
    suspend fun delete(widget: Widget)
    suspend fun findWidgetsForProfile(profileId: String): List<Widget>
}
