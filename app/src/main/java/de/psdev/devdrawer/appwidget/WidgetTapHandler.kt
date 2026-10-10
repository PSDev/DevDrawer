package de.psdev.devdrawer.appwidget

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.net.toUri
import de.psdev.devdrawer.R
import de.psdev.devdrawer.receivers.UpdateReceiver
import de.psdev.devdrawer.utils.Constants
import mu.KLogging

/** Carries out a tap on a widget row, and tells the user when it can't instead of silently doing nothing. */
class WidgetTapHandler(private val context: Context) {

    companion object : KLogging()

    fun handle(packageName: String, launchType: Int, askForScreen: Boolean) {
        val applicationInfo = applicationInfoOf(packageName)
        if (applicationInfo == null) {
            // The widget is out of date: say so and reload it without the app.
            toast(R.string.app_not_installed)
            UpdateReceiver.send(context)
            return
        }
        when (launchType) {
            Constants.LAUNCH_APP -> startApp(packageName, askForScreen)
            Constants.LAUNCH_APP_DETAILS -> startAppDetails(packageName)
            Constants.LAUNCH_UNINSTALL -> if (applicationInfo.canUninstall) startUninstall(packageName) else toast(R.string.system_app_uninstall)
        }
    }

    private fun applicationInfoOf(packageName: String): ApplicationInfo? = try {
        context.packageManager.getApplicationInfo(packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    private fun startApp(packageName: String, askForScreen: Boolean) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (askForScreen || launchIntent == null) {
            start(ChooseActivityDialog.createStartIntent(context, packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            start(
                launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            )
        }
    }

    private fun startAppDetails(packageName: String) {
        start(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri())
                .addCategory(Intent.CATEGORY_DEFAULT)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    @Suppress("DEPRECATION")
    private fun startUninstall(packageName: String) {
        start(
            Intent(Intent.ACTION_UNINSTALL_PACKAGE, "package:$packageName".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun start(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            logger.warn(e) { "Nothing handles $intent" }
            toast(R.string.action_unavailable)
        }
    }

    private fun toast(@StringRes message: Int) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
