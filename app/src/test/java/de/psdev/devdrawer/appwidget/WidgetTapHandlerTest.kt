package de.psdev.devdrawer.appwidget

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.test.core.app.ApplicationProvider
import de.psdev.devdrawer.receivers.UpdateReceiver
import de.psdev.devdrawer.utils.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class WidgetTapHandlerTest {

    private val application: Application = ApplicationProvider.getApplicationContext()
    private val handler = WidgetTapHandler(application)

    private fun install(packageName: String, flags: Int = 0) {
        val info = PackageInfo().apply {
            this.packageName = packageName
            applicationInfo = ApplicationInfo().apply {
                this.packageName = packageName
                this.flags = flags
            }
        }
        shadowOf(application.packageManager).installPackage(info)
    }

    @Test
    fun `given an app that was uninstalled, when tapped in the widget, then the user is told and widgets refresh`() {
        // Given / When
        handler.handle("com.example.gone", Constants.LAUNCH_APP, askForScreen = false)

        // Then
        assertEquals("This app is no longer installed", ShadowToast.getTextOfLatestToast())
        val broadcasts = shadowOf(application).broadcastIntents
        assertTrue(broadcasts.any { it.component?.className == UpdateReceiver::class.java.name })
    }

    @Test
    fun `given a system app, when uninstall is tapped, then the user is told instead of nothing happening`() {
        // Given
        install("com.android.settings", ApplicationInfo.FLAG_SYSTEM)

        // When
        handler.handle("com.android.settings", Constants.LAUNCH_UNINSTALL, askForScreen = false)

        // Then
        assertEquals("System apps can't be uninstalled", ShadowToast.getTextOfLatestToast())
        assertNull(shadowOf(application).nextStartedActivity)
    }

    @Test
    fun `given an installed app, when uninstall is tapped, then the system uninstaller opens`() {
        // Given
        install("com.example.client")

        // When
        handler.handle("com.example.client", Constants.LAUNCH_UNINSTALL, askForScreen = false)

        // Then
        val started = shadowOf(application).nextStartedActivity
        assertEquals(Intent.ACTION_UNINSTALL_PACKAGE, started.action)
        assertEquals("package:com.example.client", started.dataString)
    }

    @Test
    fun `given an app info, when an updated system app is checked, then its updates can be uninstalled`() {
        // Given
        val updated = ApplicationInfo().apply { flags = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP }
        val system = ApplicationInfo().apply { flags = ApplicationInfo.FLAG_SYSTEM }

        // When / Then
        assertTrue(updated.canUninstall)
        assertEquals(false, system.canUninstall)
    }
}
