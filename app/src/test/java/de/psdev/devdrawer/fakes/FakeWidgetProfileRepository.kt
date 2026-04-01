package de.psdev.devdrawer.fakes

import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.profiles.IWidgetProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeWidgetProfileRepository(initialProfiles: List<WidgetProfile> = emptyList()) : IWidgetProfileRepository {
    private val profiles = MutableStateFlow(initialProfiles)

    override fun widgetProfilesFlow(): Flow<List<WidgetProfile>> = profiles.asStateFlow()

    override suspend fun delete(widgetProfile: WidgetProfile) {
        profiles.value = profiles.value.filter { it.id != widgetProfile.id }
    }

    override suspend fun findAll(): List<WidgetProfile> = profiles.value

    override suspend fun create(widgetProfile: WidgetProfile) {
        profiles.value = profiles.value + widgetProfile
    }
}
