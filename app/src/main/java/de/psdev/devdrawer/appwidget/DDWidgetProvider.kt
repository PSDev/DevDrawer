package de.psdev.devdrawer.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.net.Uri
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import de.psdev.devdrawer.receivers.UpdateReceiver
import de.psdev.devdrawer.utils.Constants
import de.psdev.devdrawer.utils.textColorForBackground
import de.psdev.devdrawer.widgets.WidgetConfigActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KLogging
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

/**
 * NOTE: Never rename this as it will break existing widgets.
 */
@AndroidEntryPoint
class DDWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var devDrawerDatabase: DevDrawerDatabase

    companion object : KLogging()

    // ==========================================================================================================================
    // AppWidgetProvider
    // ==========================================================================================================================

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Constants.ACTION_WIDGET_PINNED -> GlobalScope.launch(Dispatchers.IO) {
                val widgetDao = devDrawerDatabase.widgetDao()
                val widgetProfileDao = devDrawerDatabase.widgetProfileDao()
                val defaultWidgetProfile = widgetProfileDao.findAll().firstOrNull()
                    ?: WidgetProfile(name = "Default").also {
                        widgetProfileDao.insert(it)
                    }
                val widgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )

                // Create entries in database
                val widget = Widget(
                    id = widgetId,
                    name = "Widget $widgetId",
                    color = Color.BLACK,
                    profileId = defaultWidgetProfile.id
                )
                widgetDao.insert(widget)
                UpdateReceiver.send(context)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        GlobalScope.launch(Dispatchers.IO) {
            for (appWidgetId in appWidgetIds) {
                val widget = devDrawerDatabase.widgetDao().findById(appWidgetId)
                if (widget != null) {
                    updateWidget(context, widget, appWidgetManager)
                } else {
                    logger.warn { "Widget $appWidgetId does not exist" }
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        logger.warn { "Deleted widgets ${appWidgetIds.joinToString()}" }
        GlobalScope.launch(Dispatchers.IO) {
            devDrawerDatabase.widgetDao().deleteByIds(appWidgetIds.toList())
        }
    }

    private fun updateWidget(context: Context, widget: Widget, appWidgetManager: AppWidgetManager) {
        try {
            val view = createRemoteViews(context, widget)
            appWidgetManager.updateAppWidget(widget.id, view)
            appWidgetManager.notifyAppWidgetViewDataChanged(widget.id, R.id.listView)
        } catch (e: Exception) {
            logger.warn(e) { "Error updating widget: ${widget.id}: ${e.message}" }
        }
    }

    private fun createRemoteViews(context: Context, widget: Widget): RemoteViews {
        // Setup the widget, and data source / adapter
        val widgetView = RemoteViews(context.packageName, R.layout.widget_layout)
        val widgetColor = widget.color
        val contrastColor = widgetColor.textColorForBackground()

        // Set background color for widget
        widgetView.setInt(R.id.container_actions, "setBackgroundColor", widgetColor)

        widgetView.setTextViewText(R.id.txt_title, widget.name)
        widgetView.setTextColor(R.id.txt_title, contrastColor)
        widgetView.setTextViewText(R.id.txt_last_updated, DateFormat.getTimeInstance().format(Date()))
        widgetView.setTextColor(R.id.txt_last_updated, contrastColor)

        val reloadPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, UpdateReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        widgetView.setOnClickPendingIntent(R.id.btn_reload, reloadPendingIntent)

        val configActivityIntent = WidgetConfigActivity.createStartIntent(context, widget.id)
        configActivityIntent.putExtra("from_widget", true)
        configActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK)
        val configActivityPendingIntent =
            PendingIntent.getActivity(context, 0, configActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        widgetView.setOnClickPendingIntent(R.id.btn_settings, configActivityPendingIntent)

        // Apps list
        val appListServiceIntent = Intent(context, WidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.id)
            putExtra("viewId", R.id.listView)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        widgetView.setRemoteAdapter(R.id.listView, appListServiceIntent)

        val clickIntent = Intent(context, ClickHandlingActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
        }
        val clickPI = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        widgetView.setPendingIntentTemplate(R.id.listView, clickPI)
        return widgetView
    }

}