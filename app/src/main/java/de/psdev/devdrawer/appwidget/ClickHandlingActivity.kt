package de.psdev.devdrawer.appwidget

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.R
import de.psdev.devdrawer.settings.PreferenceKeys
import de.psdev.devdrawer.utils.Constants
import mu.KLogging
import javax.inject.Inject

@AndroidEntryPoint
class ClickHandlingActivity : FragmentActivity() {
    companion object : KLogging() {
        const val EXTRA_PACKAGE_NAME = "packageName"
        const val EXTRA_LAUNCH_TYPE = "launchType"
    }

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    // ==========================================================================================================================
    // Android Lifecycle
    // ==========================================================================================================================

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        val launchType = intent.getIntExtra(EXTRA_LAUNCH_TYPE, 0)

        if (packageName != null && isAppInstalled(packageName)) {
            when (launchType) {
                Constants.LAUNCH_APP -> startApp(packageName)
                Constants.LAUNCH_APP_DETAILS -> startAppDetails(packageName)
                Constants.LAUNCH_UNINSTALL -> startUninstall(packageName)
            }
        }
        // We need to always call finish
        finish()
    }

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    private fun isAppInstalled(uri: String): Boolean = try {
        packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private fun startApp(packageName: String) {
        if (sharedPreferences.getBoolean(
                PreferenceKeys.SHOW_ACTIVITY_CHOICE,
                resources.getBoolean(R.bool.pref_show_activity_choice_default)
            )
        ) {
            // Show the activity choice dialog
            showActivityChooser(packageName)
        } else {
            // Launch the app
            try {
                val intent = packageManager.getLaunchIntentForPackage(packageName)!!.apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
                startActivity(intent)
            } catch (e: NullPointerException) {
                showActivityChooser(packageName)
            }
        }
    }

    private fun showActivityChooser(packageName: String) {
        val intent = ChooseActivityDialog.createStartIntent(this, packageName).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun startAppDetails(packageName: String) {
        // Launch the app details settings screen for the app
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        startActivity(intent)
    }

    @Suppress("DEPRECATION")
    private fun startUninstall(packageName: String) {
        try {
            val packageUri = Uri.parse("package:$packageName")
            val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(uninstallIntent)
        } catch (e: ActivityNotFoundException) {
            logger.warn { "Application cannot be uninstalled / possibly system app" }
        }

    }

}
