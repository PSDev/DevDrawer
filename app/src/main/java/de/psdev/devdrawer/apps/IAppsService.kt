package de.psdev.devdrawer.apps

import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.PackageHashInfo

interface IAppsService {

    /** Installed packages with their signature hashes, optionally without system apps. */
    suspend fun installedPackages(includeSystemApps: Boolean = true): List<PackageHashInfo>

    /** Labels and icons for [packages]; packages that can no longer be read are left out. */
    suspend fun appInfos(packages: List<PackageHashInfo>): List<AppInfo>
}
