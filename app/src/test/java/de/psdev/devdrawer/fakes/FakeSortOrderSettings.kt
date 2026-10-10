package de.psdev.devdrawer.fakes

import de.psdev.devdrawer.appwidget.SortOrder
import de.psdev.devdrawer.settings.ISortOrderSettings

class FakeSortOrderSettings(var sortOrder: SortOrder = SortOrder.LAST_UPDATED) : ISortOrderSettings {
    override fun defaultSortOrder(): SortOrder = sortOrder
}
