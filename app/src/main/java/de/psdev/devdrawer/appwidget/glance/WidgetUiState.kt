package de.psdev.devdrawer.appwidget.glance

import android.graphics.Bitmap
import de.psdev.devdrawer.database.WidgetHeaderColor

/** Everything the widget draws, already resolved (formatted time, scaled icons). */
data class WidgetUiState(
    val appWidgetId: Int,
    val title: String,
    val headerColor: WidgetHeaderColor,
    val updatedAt: String,
    val apps: List<WidgetAppItem>,
    /** Matching apps beyond the ones listed, which the widget only counts (see DevDrawerGlanceWidget.MAX_LISTED_APPS). */
    val hiddenAppCount: Int = 0
)

data class WidgetAppItem(
    val name: String,
    val packageName: String,
    /** Scaled to the row's icon size so the widget's RemoteViews stay small; null shows a placeholder. */
    val icon: Bitmap?
)
