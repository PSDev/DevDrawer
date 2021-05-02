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
    get() = applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1