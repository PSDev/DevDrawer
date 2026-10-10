package de.psdev.devdrawer.settings

import android.app.Application
import android.content.SharedPreferences
import de.psdev.devdrawer.appwidget.SortOrder
import javax.inject.Inject

/** The sort order Settings sets for widgets that don't have their own. */
interface ISortOrderSettings {
    fun defaultSortOrder(): SortOrder
}

class SharedPreferencesSortOrderSettings @Inject constructor(
    private val application: Application,
    private val sharedPreferences: SharedPreferences
) : ISortOrderSettings {
    override fun defaultSortOrder(): SortOrder = sharedPreferences.defaultSortOrder(application)
}
