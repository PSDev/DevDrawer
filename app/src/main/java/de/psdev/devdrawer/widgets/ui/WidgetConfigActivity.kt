package de.psdev.devdrawer.widgets.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.BaseActivity
import de.psdev.devdrawer.R
import de.psdev.devdrawer.analytics.Events
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.databinding.ActivityWidgetConfigBinding
import de.psdev.devdrawer.widgets.ui.editor.WidgetEditFragmentArgs
import mu.KLogging
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigActivity: BaseActivity() {

    companion object: KLogging() {
        fun createStartIntent(context: Context, appWidgetId: Int): Intent =
            Intent(context, WidgetConfigActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
    }

    // Dependencies
    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    private lateinit var binding: ActivityWidgetConfigBinding

    // ==========================================================================================================================
    // Activity Lifecycle
    // ==========================================================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED)

        val widgetId = getWidgetId()
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        if (intent.getBooleanExtra("from_widget", false)) {
            trackingService.trackAction(Events.EVENT_WIDGET_OPEN_SETTINGS)
        }

        binding = ActivityWidgetConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(
            R.navigation.nav_config_widget,
            WidgetEditFragmentArgs.Builder(widgetId).build().toBundle()
        )
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    // ==========================================================================================================================
    // Public API
    // ==========================================================================================================================

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, null)
        super.onBackPressed()
    }

    private fun getWidgetId(): Int =
        intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

}