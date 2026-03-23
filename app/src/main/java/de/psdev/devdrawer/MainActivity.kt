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
    companion object : KLogging()

    // Holds intents delivered via onNewIntent so they can be handled inside the Compose tree.
    private val newIntent = mutableStateOf<Intent?>(null)

    // ==========================================================================================================================
    // Android Lifecycle
    // ==========================================================================================================================

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                trackingService = trackingService
            )
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                trackingService.checkOptIn()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        newIntent.value = intent
    }

    private fun handleIntent(intent: Intent, navigator: Navigator) {
        val widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        if (widgetId != INVALID_APPWIDGET_ID) {
            navigator.navigate(WidgetEditorRoute(widgetId))
        }
    }
}
