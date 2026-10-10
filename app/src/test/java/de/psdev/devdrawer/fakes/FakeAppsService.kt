package de.psdev.devdrawer.fakes

import android.graphics.drawable.ColorDrawable
import de.psdev.devdrawer.apps.IAppsService
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.PackageHashInfo

/** Installed packages with display names; needs Robolectric for the icon drawables. */
class FakeAppsService(private val apps: Map<PackageHashInfo, String>) : IAppsService {

    override suspend fun installedPackages(includeSystemApps: Boolean): List<PackageHashInfo> = apps.keys.toList()

    override suspend fun appInfos(packages: List<PackageHashInfo>): List<AppInfo> = packages.map { info ->
        AppInfo(
            name = apps.getValue(info),
            packageName = info.packageName,
            appIcon = ColorDrawable(0),
            firstInstallTime = info.firstInstallTime,
            lastUpdateTime = info.lastUpdateTime,
            signatureHashSha256 = info.signatureHashSha256
        )
    }
}
