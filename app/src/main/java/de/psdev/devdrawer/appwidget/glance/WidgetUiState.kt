package de.psdev.devdrawer.appwidget.glance

import android.graphics.Bitmap
import de.psdev.devdrawer.database.WidgetHeaderColor

/** Everything the widget draws, already resolved (formatted time, scaled icons). */
data class WidgetUiState(
    val appWidgetId: Int,
    val title: String,
    val headerColor: WidgetHeaderColor,
    val updatedAt: String,
    val apps: List<WidgetAppItem>
)

data class WidgetAppItem(
    val name: String,
    val packageName: String,
    /** Scaled to the row's icon size so the widget's RemoteViews stay small; null shows a placeholder. */
    val icon: Bitmap?
)
