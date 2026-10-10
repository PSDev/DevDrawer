package de.psdev.devdrawer.appwidget

import de.psdev.devdrawer.apps.IAppsService
import de.psdev.devdrawer.apps.comparator
import de.psdev.devdrawer.apps.matching
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.profiles.IPackageFilterRepository
import de.psdev.devdrawer.widgets.IWidgetRepository

/** What a home-screen widget shows: its settings and the apps its profile matches, in the widget's sort order. */
data class WidgetContent(
    val widget: Widget,
    val apps: List<AppInfo>
)

class WidgetContentLoader(
    private val widgetRepository: IWidgetRepository,
    private val packageFilterRepository: IPackageFilterRepository,
    private val appsService: IAppsService,
    private val defaultSortOrder: suspend () -> SortOrder
) {

    /** Null when no widget with [widgetId] is saved yet (just placed, setup not finished). */
    suspend fun load(widgetId: Int): WidgetContent? {
        val widget = widgetRepository.findById(widgetId) ?: return null
        val filters = packageFilterRepository.findAllByProfile(widget.profileId)
        val packages = appsService.installedPackages(includeSystemApps = true).matching(filters)
        val sortOrder = widget.sortOrder ?: defaultSortOrder()
        val apps = appsService.appInfos(packages).sortedWith(sortOrder.comparator())
        return WidgetContent(widget = widget, apps = apps)
    }
}
