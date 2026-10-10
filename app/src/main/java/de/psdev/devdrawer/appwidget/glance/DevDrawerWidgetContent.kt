package de.psdev.devdrawer.appwidget.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import de.psdev.devdrawer.MainActivity
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.ClickHandlingActivity
import de.psdev.devdrawer.ui.theme.dark
import de.psdev.devdrawer.ui.theme.light
import de.psdev.devdrawer.utils.Constants

private val PackageNameKey = ActionParameters.Key<String>(ClickHandlingActivity.EXTRA_PACKAGE_NAME)
private val LaunchTypeKey = ActionParameters.Key<Int>(ClickHandlingActivity.EXTRA_LAUNCH_TYPE)

@Composable
fun DevDrawerWidgetContent(state: WidgetUiState) {
    val context = LocalContext.current
    val background = GlanceModifier
        .fillMaxSize()
        .appWidgetBackground()
        .background(GlanceTheme.colors.widgetBackground)
    val rounded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        background.cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        background.cornerRadius(16.dp)
    }
    Column(modifier = rounded) {
        Header(state)
        if (state.apps.isEmpty()) {
            EmptyState(context, state.appWidgetId)
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(state.apps, itemId = { it.packageName.hashCode().toLong() }) { app ->
                    AppRow(context, app)
                }
            }
        }
    }
}

@Composable
private fun Header(state: WidgetUiState) {
    val context = LocalContext.current
    val light = state.headerColor.light
    val dark = state.headerColor.dark
    val container = if (light != null && dark != null) ColorProvider(light.container, dark.container) else GlanceTheme.colors.primaryContainer
    val content = if (light != null && dark != null) ColorProvider(light.content, dark.content) else GlanceTheme.colors.onPrimaryContainer
    val count = if (state.apps.isEmpty()) {
        context.getString(R.string.widget_no_apps)
    } else {
        context.resources.getQuantityString(R.plurals.widget_app_count, state.apps.size, state.apps.size)
    }
    Row(
        modifier = GlanceModifier.fillMaxWidth().background(container).padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = state.title,
                maxLines = 1,
                style = TextStyle(color = content, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            )
            Text(
                text = context.getString(R.string.widget_subtitle, count, state.updatedAt),
                maxLines = 1,
                style = TextStyle(color = content, fontSize = 12.sp)
            )
        }
        Image(
            provider = ImageProvider(R.drawable.ic_baseline_refresh_24),
            contentDescription = context.getString(R.string.reload),
            colorFilter = ColorFilter.tint(content),
            modifier = GlanceModifier.size(48.dp).padding(12.dp).clickable(actionRunCallback<RefreshAction>())
        )
        Image(
            provider = ImageProvider(R.drawable.ic_baseline_settings_24),
            contentDescription = context.getString(R.string.widget_settings),
            colorFilter = ColorFilter.tint(content),
            modifier = GlanceModifier.size(48.dp).padding(12.dp)
                .clickable(actionStartActivity(mainActivityIntent(context, state.appWidgetId, openSetup = false)))
        )
    }
}

@Composable
private fun EmptyState(context: Context, appWidgetId: Int) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = context.getString(R.string.widget_empty_title),
            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = context.getString(R.string.widget_empty_text),
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 14.sp)
        )
        Spacer(GlanceModifier.height(12.dp))
        Button(
            text = context.getString(R.string.widget_choose_apps),
            onClick = actionStartActivity(mainActivityIntent(context, appWidgetId, openSetup = true)),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = GlanceTheme.colors.primary,
                contentColor = GlanceTheme.colors.onPrimary
            )
        )
    }
}

@Composable
private fun AppRow(context: Context, app: WidgetAppItem) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
            .clickable(clickHandlingAction(app.packageName, Constants.LAUNCH_APP)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = app.icon?.let { ImageProvider(it) } ?: ImageProvider(R.drawable.ic_baseline_widgets_24),
            contentDescription = null,
            modifier = GlanceModifier.size(40.dp)
        )
        Column(modifier = GlanceModifier.defaultWeight().padding(start = 12.dp)) {
            Text(
                text = app.name,
                maxLines = 1,
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 16.sp)
            )
            Text(
                text = app.packageName,
                maxLines = 1,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
            )
        }
        Image(
            provider = ImageProvider(R.drawable.ic_baseline_delete_24),
            contentDescription = context.getString(R.string.uninstall_app, app.name),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
            modifier = GlanceModifier.size(48.dp).padding(12.dp)
                .clickable(clickHandlingAction(app.packageName, Constants.LAUNCH_UNINSTALL))
        )
        Image(
            provider = ImageProvider(R.drawable.ic_baseline_info_24),
            contentDescription = context.getString(R.string.app_details_for, app.name),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
            modifier = GlanceModifier.size(48.dp).padding(12.dp)
                .clickable(clickHandlingAction(app.packageName, Constants.LAUNCH_APP_DETAILS))
        )
    }
}

private fun clickHandlingAction(packageName: String, launchType: Int) = actionStartActivity<ClickHandlingActivity>(
    actionParametersOf(PackageNameKey to packageName, LaunchTypeKey to launchType)
)

private fun mainActivityIntent(context: Context, appWidgetId: Int, openSetup: Boolean): Intent =
    Intent(context, MainActivity::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        putExtra(MainActivity.EXTRA_OPEN_SETUP, openSetup)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
