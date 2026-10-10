package de.psdev.devdrawer.profiles

import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.WidgetProfile
import kotlinx.coroutines.flow.Flow

interface IPackageFilterRepository {
    suspend fun getById(packageFilterId: String): PackageFilter?
    suspend fun delete(packageFilter: PackageFilter)
    suspend fun save(packageFilter: PackageFilter)
    suspend fun saveProfile(widgetProfile: WidgetProfile, filters: List<PackageFilter>)
    suspend fun findAllByProfile(profileId: String): List<PackageFilter>
    fun allFiltersFlow(): Flow<List<PackageFilter>>
}
