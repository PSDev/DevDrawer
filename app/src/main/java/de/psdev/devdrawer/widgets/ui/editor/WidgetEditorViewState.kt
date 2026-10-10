package de.psdev.devdrawer.widgets.ui.editor

import androidx.compose.runtime.Immutable
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.profiles.ProfileWithAppCount

@Immutable
data class WidgetEditorViewState(
    val persistedWidget: Widget? = null,
    val editableWidget: Widget? = null,
    val profiles: List<ProfileWithAppCount> = emptyList(),
    /** The first apps the widget would list with the edited settings. */
    val previewApps: List<AppInfo> = emptyList(),
    /** How many apps the widget would list with the edited settings. */
    val previewAppCount: Int = 0,
    /** The Settings sort order, used while the widget has none of its own. */
    val defaultSortOrder: SortOrder = SortOrder.LAST_UPDATED
) {
    val isDirty: Boolean
        get() = editableWidget != null && editableWidget != persistedWidget

    companion object {
        val Empty = WidgetEditorViewState()
    }
}
