package de.psdev.devdrawer.profiles.ui.editor

import androidx.compose.runtime.Immutable
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.WidgetProfile

@Immutable
data class WidgetProfileEditorViewState(
    val widgetProfile: WidgetProfile? = null,
    val widgetName: String? = null,
    val packageFilters: List<PackageFilter> = emptyList(),
    val isDirty: Boolean = false,
    /** Installed apps the edited filters match, by name. */
    val matchingApps: List<AppInfo> = emptyList(),
    /** How many installed apps each filter matches on its own, by filter id. */
    val filterAppCounts: Map<String, Int> = emptyMap()
) {
    companion object {
        val Empty = WidgetProfileEditorViewState()
    }
}
