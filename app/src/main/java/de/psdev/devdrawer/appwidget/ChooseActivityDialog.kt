package de.psdev.devdrawer.appwidget

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.psdev.devdrawer.R
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import mu.KLogging

/** Lets the user pick which screen of an app to open from the widget; opens it directly when there is only one. */
class ChooseActivityDialog : ComponentActivity() {

    companion object : KLogging() {
        const val EXTRA_PACKAGE_NAME = "packageName"

        @JvmStatic
        fun createStartIntent(context: Context, packageName: String) = Intent(context, ChooseActivityDialog::class.java).apply {
            putExtra(EXTRA_PACKAGE_NAME, packageName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        val screens = appPackageName?.let(::screensOf)
        when {
            appPackageName == null || screens == null -> finish()
            screens.isEmpty() -> {
                Toast.makeText(this, R.string.no_activities, Toast.LENGTH_LONG).show()
                finish()
            }
            screens.size == 1 -> open(appPackageName, screens.single())
            else -> {
                val appInfo = packageManager.getApplicationInfo(appPackageName, 0)
                val appName = appInfo.loadLabel(packageManager).toString()
                val appIcon = appInfo.loadIcon(packageManager)
                setContent {
                    DevDrawerTheme {
                        ChooseScreenSheet(
                            appName = appName,
                            appIcon = appIcon,
                            screens = screens,
                            onScreenSelected = { open(appPackageName, it) },
                            onDismiss = ::finish
                        )
                    }
                }
            }
        }
    }

    private fun screensOf(appPackageName: String): List<AppScreen>? = try {
        val packageInfo = packageManager.getPackageInfo(appPackageName, PackageManager.GET_ACTIVITIES)
        val mainClassName = packageManager.getLaunchIntentForPackage(appPackageName)?.component?.className
        appScreens(appPackageName, packageInfo.activities.orEmpty().toList(), mainClassName) {
            it.loadLabel(packageManager).toString()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    private fun open(appPackageName: String, screen: AppScreen) {
        val intent = Intent().apply {
            component = ComponentName(appPackageName, screen.className)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            logger.warn(e) { "Cannot open ${screen.className}" }
            Toast.makeText(this, R.string.screen_open_failed, Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            logger.warn(e) { "Not allowed to open ${screen.className}" }
            Toast.makeText(this, R.string.screen_open_failed, Toast.LENGTH_LONG).show()
        }
        finish()
    }
}
