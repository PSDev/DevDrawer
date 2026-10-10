package de.psdev.devdrawer.appwidget.glance

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.text.format.DateFormat
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.material3.ColorProviders
import dagger.hilt.android.EntryPointAccessors
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.WidgetContent
import de.psdev.devdrawer.appwidget.WidgetEntryPoint
import de.psdev.devdrawer.database.WidgetHeaderColor
import de.psdev.devdrawer.ui.theme.DarkColorScheme
import de.psdev.devdrawer.ui.theme.LightColorScheme
import java.util.Date

/** The home-screen widget: a header with the widget's name, then the apps its profile matches. */
class DevDrawerGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val loader = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java).widgetContentLoader()
        val state = loader.load(appWidgetId).toUiState(context, appWidgetId)
        val useDynamicColors = state.headerColor == WidgetHeaderColor.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        provideContent {
            if (useDynamicColors) {
                GlanceTheme { DevDrawerWidgetContent(state) }
            } else {
                GlanceTheme(colors = AppColors) { DevDrawerWidgetContent(state) }
            }
        }
    }

    private companion object {
        val AppColors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)

        const val ICON_SIZE_DP = 40
    }

    /** A widget whose setup isn't finished yet shows its empty state under the app's name. */
    private fun WidgetContent?.toUiState(context: Context, appWidgetId: Int): WidgetUiState = WidgetUiState(
        appWidgetId = appWidgetId,
        title = this?.widget?.name ?: context.getString(R.string.app_name),
        headerColor = this?.widget?.headerColor ?: WidgetHeaderColor.AMBER,
        updatedAt = DateFormat.getTimeFormat(context).format(Date()),
        apps = this?.apps.orEmpty().map { it.toItem(context) }
    )

    private fun AppInfo.toItem(context: Context): WidgetAppItem = WidgetAppItem(
        name = name,
        packageName = packageName,
        icon = scaledIcon(context)
    )

    /** Every icon travels in the widget's RemoteViews, so it is scaled to the 40 dp it is drawn at. */
    private fun AppInfo.scaledIcon(context: Context): Bitmap? {
        val size = (ICON_SIZE_DP * context.resources.displayMetrics.density).toInt()
        return runCatching { appIcon.toBitmap(size, size) }.getOrNull()
    }
}
