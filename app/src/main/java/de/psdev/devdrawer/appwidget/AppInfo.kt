package de.psdev.devdrawer.appwidget

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import mu.KotlinLogging
import okio.HashingSink
import okio.blackholeSink
import okio.buffer

val logger = KotlinLogging.logger("AppInfo")

data class AppInfo(
    val name: String,
    val packageName: String,
    val appIcon: Drawable,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val signatureHashSha256: String,
    val versionName: String = "",
    val versionCode: Long = 0
)

fun PackageHashInfo.toAppInfo(context: Context): AppInfo? = try {
    val packageManager = context.packageManager
    val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    val applicationInfo = packageInfo.applicationInfo!!
    val appName = applicationInfo.loadLabel(packageManager).toString()
    val appIcon = applicationInfo.loadIcon(packageManager)
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()
    }
    AppInfo(
        name = appName,
        packageName = packageName,
        appIcon = appIcon,
        firstInstallTime = firstInstallTime,
        lastUpdateTime = lastUpdateTime,
        signatureHashSha256 = signatureHashSha256,
        versionName = packageInfo.versionName ?: "",
        versionCode = versionCode
    )
} catch (e: Exception) {
    logger.warn(e) { "Error: ${e.message}" }
    null
}

@Suppress("DEPRECATION")
val PackageInfo.signatureHashSha256: String
    get() {
        val hashingSink = HashingSink.sha256(blackholeSink()).use {
            it.buffer().use { bufferedSink ->
                val signatureBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()
                } else {
                    signatures?.firstOrNull()?.toByteArray()
                } ?: return ""
                bufferedSink.write(signatureBytes)
            }
            it
        }
        return hashingSink.hash.hex()
    }