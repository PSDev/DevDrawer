package de.psdev.devdrawer.apps

import android.app.Application
import android.content.pm.PackageManager.GET_SIGNATURES
import android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.perf.performance
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.PackageHashInfo
import de.psdev.devdrawer.appwidget.isSystemApp
import de.psdev.devdrawer.appwidget.toAppInfo
import de.psdev.devdrawer.appwidget.toPackageHashInfo
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.utils.trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppsService @Inject constructor(
    private val application: Application
) {

    private val packageManager by lazy { application.packageManager }

    suspend fun getAppsForPackageFilter(
        packageFilter: PackageFilter
    ): List<AppInfo> = Firebase.performance.trace("getAppsForPackageFilter") {
        withContext(Dispatchers.IO) {
            packageManager.getInstalledPackages(getFlags())
                .asSequence()
                .map { it.toPackageHashInfo() }
                .filter { packageFilter.matches(it) }
                .mapNotNull { it.toAppInfo(application) }
                .sortedBy { it.name }
                .toList()
        }
    }

    suspend fun getInstalledPackages(systemApps: Boolean = false): List<PackageHashInfo> =
        Firebase.performance.trace("getInstalledPackages") {
            withContext(Dispatchers.IO) {
                packageManager.getInstalledPackages(getFlags())
                    .asSequence()
                    .filter {
                        if (systemApps) {
                            true
                        } else !it.isSystemApp
                    }
                    .map { it.toPackageHashInfo() }
                    .toList()
            }
        }

    @Suppress("DEPRECATION")
    private fun getFlags() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        GET_SIGNING_CERTIFICATES
    } else {
        GET_SIGNATURES
    }

}
