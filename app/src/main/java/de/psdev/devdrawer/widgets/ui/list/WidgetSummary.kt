package de.psdev.devdrawer.widgets.ui.list

import androidx.compose.runtime.Immutable
import de.psdev.devdrawer.database.Widget

/** A widget as the Widgets list shows it: with its profile's name and how many apps it lists. */
@Immutable
data class WidgetSummary(
    val widget: Widget,
    val profileName: String,
    val appCount: Int
)
