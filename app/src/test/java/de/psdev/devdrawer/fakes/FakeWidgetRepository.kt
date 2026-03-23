package de.psdev.devdrawer.fakes

import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.widgets.IWidgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeWidgetRepository(initialWidgets: List<Widget> = emptyList()) : IWidgetRepository {
    private val widgets = MutableStateFlow(initialWidgets)

    override fun widgetFlow(widgetId: Int): Flow<Widget?> =
        widgets.map { list -> list.find { it.id == widgetId } }

    override suspend fun update(widget: Widget) {
        widgets.value = widgets.value.map { if (it.id == widget.id) widget else it }
    }

    override suspend fun delete(widget: Widget) {
        widgets.value = widgets.value.filter { it.id != widget.id }
    }

    override suspend fun findWidgetsForProfile(profileId: String): List<Widget> =
        widgets.value.filter { it.profileId == profileId }
}
