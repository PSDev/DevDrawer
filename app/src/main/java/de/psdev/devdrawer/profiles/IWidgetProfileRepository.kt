package de.psdev.devdrawer.profiles

import de.psdev.devdrawer.database.WidgetProfile
import kotlinx.coroutines.flow.Flow

interface IWidgetProfileRepository {
    fun widgetProfilesFlow(): Flow<List<WidgetProfile>>
    suspend fun delete(widgetProfile: WidgetProfile)
    suspend fun findAll(): List<WidgetProfile>
    suspend fun create(widgetProfile: WidgetProfile)
}
