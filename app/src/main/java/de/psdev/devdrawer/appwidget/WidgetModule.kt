package de.psdev.devdrawer.appwidget

import android.app.Application
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.psdev.devdrawer.apps.IAppsService
import de.psdev.devdrawer.profiles.IPackageFilterRepository
import de.psdev.devdrawer.settings.defaultSortOrder
import de.psdev.devdrawer.widgets.IWidgetRepository

@Module
@InstallIn(SingletonComponent::class)
class WidgetModule {

    @Provides
    fun widgetContentLoader(
        application: Application,
        widgetRepository: IWidgetRepository,
        packageFilterRepository: IPackageFilterRepository,
        appsService: IAppsService,
        sharedPreferences: SharedPreferences
    ): WidgetContentLoader = WidgetContentLoader(
        widgetRepository = widgetRepository,
        packageFilterRepository = packageFilterRepository,
        appsService = appsService,
        defaultSortOrder = { sharedPreferences.defaultSortOrder(application) }
    )
}

/** Hilt access for the Glance widget, which the framework creates without injection. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetContentLoader(): WidgetContentLoader
}
