package de.psdev.devdrawer.profiles

import android.app.Application
import android.content.pm.PackageManager.GET_SIGNATURES
import android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
import android.os.Build
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import de.psdev.devdrawer.appwidget.*
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
                .filterNot { it.isSystemApp } // TODO Option to allow system apps?
                .map { it.toPackageHashInfo() }
                .filter { packageFilter.matches(it) }
                .mapNotNull { it.toAppInfo(application) }
                .sortedBy { it.name }
                .toList()
        }
    }

    suspend fun getInstalledPackages(): List<PackageHashInfo> = Firebase.performance.trace("getInstalledPackages") {
        withContext(Dispatchers.IO) {
            packageManager.getInstalledPackages(getFlags())
                .asSequence()
                .filterNot { it.isSystemApp } // TODO Option to allow system apps?
                .map { it.toPackageHashInfo() }
                .toList()
        }
    }

    private fun getFlags() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        GET_SIGNING_CERTIFICATES
    } else {
        GET_SIGNATURES
    }

}