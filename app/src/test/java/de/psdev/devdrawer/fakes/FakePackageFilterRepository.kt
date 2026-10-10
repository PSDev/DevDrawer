package de.psdev.devdrawer.fakes

import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.profiles.IPackageFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePackageFilterRepository(initialFilters: List<PackageFilter> = emptyList()) : IPackageFilterRepository {

    val filters = MutableStateFlow(initialFilters)

    override suspend fun getById(packageFilterId: String): PackageFilter? = filters.value.find { it.id == packageFilterId }

    override suspend fun delete(packageFilter: PackageFilter) {
        filters.value = filters.value.filter { it.id != packageFilter.id }
    }

    override suspend fun save(packageFilter: PackageFilter) {
        filters.value = filters.value.filter { it.id != packageFilter.id } + packageFilter
    }

    override suspend fun saveProfile(widgetProfile: WidgetProfile, filters: List<PackageFilter>) {
        this.filters.value = this.filters.value.filter { it.profileId != widgetProfile.id } + filters
    }

    override suspend fun findAllByProfile(profileId: String): List<PackageFilter> =
        filters.value.filter { it.profileId == profileId }

    override fun allFiltersFlow(): Flow<List<PackageFilter>> = filters.map { it }
}
