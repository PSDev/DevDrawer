package de.psdev.devdrawer.database

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.psdev.devdrawer.profiles.IPackageFilterRepository
import de.psdev.devdrawer.profiles.IWidgetProfileRepository
import de.psdev.devdrawer.profiles.PackageFilterRepository
import de.psdev.devdrawer.profiles.WidgetProfileRepository
import de.psdev.devdrawer.widgets.IWidgetRepository
import de.psdev.devdrawer.widgets.WidgetRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindWidgetRepository(impl: WidgetRepository): IWidgetRepository

    @Binds
    abstract fun bindWidgetProfileRepository(impl: WidgetProfileRepository): IWidgetProfileRepository

    @Binds
    abstract fun bindPackageFilterRepository(impl: PackageFilterRepository): IPackageFilterRepository

}
