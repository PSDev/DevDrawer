package de.psdev.devdrawer

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import mu.KLogging

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    companion object : KLogging() {
        /** Set by the widget's empty state: open the widget's setup instead of its editor. */
        const val EXTRA_OPEN_SETUP = "open_setup"

        private const val STATE_EXTERNAL_SETUP_WIDGET_ID = "external_setup_widget_id"
        private const val STATE_IS_CONFIGURATION = "is_configuration"
    }

    /** The widget whose setup was opened from outside the app (launcher or widget); the activity closes when it's done. */
    private var externalSetupWidgetId: Int = INVALID_APPWIDGET_ID

    /** True while the launcher waits for this activity's result to place or reconfigure [externalSetupWidgetId]. */
    private var isConfiguration: Boolean = false

    // Holds intents delivered via onNewIntent so they can be handled inside the Compose tree.
    private val newIntent = mutableStateOf<Intent?>(null)

    // ==========================================================================================================================
    // Android Lifecycle
    // ==========================================================================================================================

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            externalSetupWidgetId = savedInstanceState.getInt(STATE_EXTERNAL_SETUP_WIDGET_ID, INVALID_APPWIDGET_ID)
            isConfiguration = savedInstanceState.getBoolean(STATE_IS_CONFIGURATION, false)
            if (isConfiguration) setConfigurationResult(RESULT_CANCELED)
        }
        enableEdgeToEdge()
        setContent {
            val navigationState = rememberNavigationState(
                startRoute = WidgetListRoute,
                topLevelRoutes = topLevelRoutes.toSet()
            )
            val navigator = remember { Navigator(navigationState) }

            // Handle the launch intent only on a fresh start, not on config changes.
            if (savedInstanceState == null) {
                LaunchedEffect(Unit) {
                    handleIntent(intent, navigator)
                }
            }

            // Handle intents delivered while the app is already running.
            val pendingIntent = newIntent.value
            LaunchedEffect(pendingIntent) {
                pendingIntent?.let {
                    handleIntent(it, navigator)
                    newIntent.value = null
                }
            }

            DevDrawerApp(
                navigationState = navigationState,
                navigator = navigator,
                trackingService = trackingService,
                onWidgetSetupDone = { widget -> leaveWidgetSetup(widget.id, navigator, done = true) },
                onWidgetSetupBack = { widgetId -> leaveWidgetSetup(widgetId, navigator, done = false) }
            )
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                trackingService.checkOptIn()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_EXTERNAL_SETUP_WIDGET_ID, externalSetupWidgetId)
        outState.putBoolean(STATE_IS_CONFIGURATION, isConfiguration)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        newIntent.value = intent
    }

    private fun handleIntent(intent: Intent, navigator: Navigator) {
        val widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        val launch = widgetLaunchFor(
            action = intent.action,
            widgetId = widgetId,
            openSetup = intent.getBooleanExtra(EXTRA_OPEN_SETUP, false)
        ) ?: return
        if (launch.route is WidgetSetupRoute) {
            externalSetupWidgetId = widgetId
            isConfiguration = launch.isConfiguration
            // Leaving setup without finishing it removes a widget the launcher is placing.
            if (isConfiguration) setConfigurationResult(RESULT_CANCELED)
        }
        navigator.navigate(launch.route)
    }

    /** Done keeps a widget the launcher is placing; back leaves the result cancelled, which removes it. */
    private fun leaveWidgetSetup(widgetId: Int, navigator: Navigator, done: Boolean) {
        when (setupExitFor(widgetId, externalSetupWidgetId)) {
            SetupExit.FINISH_ACTIVITY -> {
                if (done && isConfiguration) setConfigurationResult(RESULT_OK)
                externalSetupWidgetId = INVALID_APPWIDGET_ID
                finish()
            }
            SetupExit.GO_BACK -> navigator.goBack()
        }
    }

    private fun setConfigurationResult(resultCode: Int) {
        setResult(resultCode, Intent().putExtra(EXTRA_APPWIDGET_ID, externalSetupWidgetId))
    }
}
