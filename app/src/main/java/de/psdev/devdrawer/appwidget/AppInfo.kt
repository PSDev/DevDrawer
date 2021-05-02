package de.psdev.devdrawer.appwidget

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.recyclerview.widget.DiffUtil
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
    val signatureHashSha256: String
) {
    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
                oldItem.packageName == newItem.packageName

            override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean = oldItem == newItem
        }
    }
}

fun PackageHashInfo.toAppInfo(context: Context): AppInfo? = try {
    val packageManager = context.packageManager
    val applicationInfo: ApplicationInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).applicationInfo
    val appName = applicationInfo.loadLabel(packageManager).toString()
    val appIcon = applicationInfo.loadIcon(packageManager)
    AppInfo(appName, packageName, appIcon, firstInstallTime, lastUpdateTime, signatureHashSha256)
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
                    signingInfo.apkContentsSigners.first().toByteArray()
                } else {
                    signatures.first().toByteArray()
                }
                bufferedSink.write(signatureBytes)
            }
            it
        }
        return hashingSink.hash.hex()
    }