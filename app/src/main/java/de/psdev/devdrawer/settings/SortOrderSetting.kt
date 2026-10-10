package de.psdev.devdrawer.settings

import android.content.Context
import android.content.SharedPreferences
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.SortOrder

/** The sort order Settings sets for widgets that don't have their own. */
fun SharedPreferences.defaultSortOrder(context: Context): SortOrder = SortOrder.valueOf(
    getString(PreferenceKeys.SORT_ORDER, null) ?: context.getString(R.string.pref_sort_order_default)
)
