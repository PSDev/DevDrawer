package de.psdev.devdrawer.profiles

import android.app.Application
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.receivers.UpdateReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageFilterRepository @Inject constructor(
    private val application: Application,
    private val devDrawerDatabase: DevDrawerDatabase
) {

    suspend fun getById(packageFilterId: String) = devDrawerDatabase.packageFilterDao().findById(packageFilterId)

    suspend fun delete(packageFilter: PackageFilter) {
        devDrawerDatabase.packageFilterDao().delete(packageFilter)
        UpdateReceiver.send(application)
    }

    suspend fun save(packageFilter: PackageFilter) {
        devDrawerDatabase.packageFilterDao().insert(packageFilter)
        UpdateReceiver.send(application)
    }

}