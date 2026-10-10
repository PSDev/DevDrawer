package de.psdev.devdrawer.appwidget

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo

data class PackageHashInfo(
    val packageName: String,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val signatureHashSha256: String
)

fun PackageInfo.toPackageHashInfo(): PackageHashInfo = PackageHashInfo(packageName, firstInstallTime, lastUpdateTime, signatureHashSha256)
val PackageInfo.isSystemApp: Boolean
    get() = applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0

/** Regular apps can be uninstalled; of system apps only updated ones, which then lose their updates. */
val ApplicationInfo.canUninstall: Boolean
    get() = flags and ApplicationInfo.FLAG_SYSTEM == 0 || flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
