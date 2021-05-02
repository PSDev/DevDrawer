package de.psdev.devdrawer.widgets.ui.editor

import androidx.compose.runtime.Immutable
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile

@Immutable
data class WidgetEditorViewState(
    val persistedWidget: Widget? = null,
    val editableWidget: Widget? = null,
    val widgetProfiles: List<WidgetProfile> = emptyList()
) {
    companion object {
        val Empty = WidgetEditorViewState()
    }
}
