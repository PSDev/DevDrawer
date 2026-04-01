package de.psdev.devdrawer.profiles

import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.WidgetProfile
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetProfileRepository @Inject constructor(
    private val devDrawerDatabase: DevDrawerDatabase
) : IWidgetProfileRepository {

    override fun widgetProfilesFlow() = devDrawerDatabase.widgetProfileDao().findAllFlow().distinctUntilChanged()
    override suspend fun delete(widgetProfile: WidgetProfile) {
        devDrawerDatabase.widgetProfileDao().delete(widgetProfile)
    }

    override suspend fun findAll(): List<WidgetProfile> = devDrawerDatabase.widgetProfileDao().findAll()
    override suspend fun create(widgetProfile: WidgetProfile) {
        devDrawerDatabase.widgetProfileDao().insert(widgetProfile)
    }

}