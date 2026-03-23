package de.psdev.devdrawer.profiles

import android.app.Application
import androidx.room.withTransaction
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.receivers.UpdateReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageFilterRepository @Inject constructor(
    private val application: Application,
    private val devDrawerDatabase: DevDrawerDatabase
) : IPackageFilterRepository {

    override suspend fun getById(packageFilterId: String) =
        devDrawerDatabase.packageFilterDao().findById(packageFilterId)

    override suspend fun delete(packageFilter: PackageFilter) {
        devDrawerDatabase.packageFilterDao().delete(packageFilter)
        UpdateReceiver.send(application)
    }

    override suspend fun save(packageFilter: PackageFilter) {
        devDrawerDatabase.packageFilterDao().insert(packageFilter)
        UpdateReceiver.send(application)
    }

    override suspend fun saveProfile(widgetProfile: WidgetProfile, filters: List<PackageFilter>) {
        devDrawerDatabase.withTransaction {
            devDrawerDatabase.widgetProfileDao().update(widgetProfile)
            devDrawerDatabase.packageFilterDao().deleteAllByProfile(widgetProfile.id)
            devDrawerDatabase.packageFilterDao().insert(*filters.toTypedArray())
        }
        UpdateReceiver.send(application)
    }

}