package de.psdev.devdrawer.appwidget

import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.psdev.devdrawer.apps.IAppsService
import de.psdev.devdrawer.profiles.IPackageFilterRepository
import de.psdev.devdrawer.settings.ISortOrderSettings
import de.psdev.devdrawer.widgets.IWidgetRepository

@Module
@InstallIn(SingletonComponent::class)
class WidgetModule {

    @Provides
    fun widgetContentLoader(
        widgetRepository: IWidgetRepository,
        packageFilterRepository: IPackageFilterRepository,
        appsService: IAppsService,
        sortOrderSettings: ISortOrderSettings
    ): WidgetContentLoader = WidgetContentLoader(
        widgetRepository = widgetRepository,
        packageFilterRepository = packageFilterRepository,
        appsService = appsService,
        defaultSortOrder = { sortOrderSettings.defaultSortOrder() }
    )
}

/** Hilt access for the Glance widget, which the framework creates without injection. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetContentLoader(): WidgetContentLoader
}
