package de.psdev.devdrawer.profiles.ui.editor

import androidx.compose.runtime.Immutable
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.WidgetProfile

@Immutable
data class WidgetProfileEditorViewState(
    val widgetProfile: WidgetProfile? = null,
    val widgetName: String? = null,
    val packageFilters: List<PackageFilter> = emptyList()
) {
    companion object {
        val Empty = WidgetProfileEditorViewState()
    }
}
