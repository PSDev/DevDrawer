package de.psdev.devdrawer.appwidget

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.R
import de.psdev.devdrawer.settings.PreferenceKeys
import javax.inject.Inject

/** Trampoline for taps on widget rows; the widget can't start these actions itself. */
@AndroidEntryPoint
class ClickHandlingActivity : FragmentActivity() {
    companion object {
        const val EXTRA_PACKAGE_NAME = "packageName"
        const val EXTRA_LAUNCH_TYPE = "launchType"
    }

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        if (packageName != null) {
            val askForScreen = sharedPreferences.getBoolean(
                PreferenceKeys.SHOW_ACTIVITY_CHOICE,
                resources.getBoolean(R.bool.pref_show_activity_choice_default)
            )
            WidgetTapHandler(this).handle(packageName, intent.getIntExtra(EXTRA_LAUNCH_TYPE, 0), askForScreen)
        }
        finish()
    }
}
