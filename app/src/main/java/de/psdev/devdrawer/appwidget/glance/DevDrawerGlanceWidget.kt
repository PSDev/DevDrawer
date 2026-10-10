package de.psdev.devdrawer.appwidget.glance

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.material3.ColorProviders
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.EntryPointAccessors
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.AppInfo
import de.psdev.devdrawer.appwidget.WidgetContent
import de.psdev.devdrawer.appwidget.WidgetEntryPoint
import de.psdev.devdrawer.database.WidgetHeaderColor
import de.psdev.devdrawer.ui.theme.DarkColorScheme
import de.psdev.devdrawer.ui.theme.LightColorScheme
import java.util.Date
import kotlin.math.min

/** The home-screen widget: a header with the widget's name, then the apps its profile matches. */
class DevDrawerGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val loader = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java).widgetContentLoader()
        val load = suspend { loader.load(appWidgetId).toUiState(context, appWidgetId) }
        val startToken = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)[RefreshTokenKey]
        val initialState = load()
        provideContent {
            // While this session runs, update() only recomposes; a new refresh token is what reloads the apps.
            val token = currentState<Preferences>()[RefreshTokenKey]
            val state by produceState(initialState, token) {
                if (token != startToken) value = load()
            }
            if (state.headerColor == WidgetHeaderColor.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                GlanceTheme { DevDrawerWidgetContent(state) }
            } else {
                GlanceTheme(colors = AppColors) { DevDrawerWidgetContent(state) }
            }
        }
    }

    companion object {
        private val AppColors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)
        private val RefreshTokenKey = longPreferencesKey("refresh_token")

        /**
         * Every row's icon travels in the widget's RemoteViews, which must stay under the ~1 MB Binder limit:
         * the list is capped and icons are drawn at 40 dp but never stored larger than 72 px.
         */
        const val MAX_LISTED_APPS = 40
        private const val ICON_SIZE_DP = 40
        private const val MAX_ICON_SIZE_PX = 72

        /** Reloads one widget's apps, also when its Glance session is still running. */
        suspend fun refresh(context: Context, glanceId: GlanceId) {
            updateAppWidgetState(context, glanceId) { it[RefreshTokenKey] = System.nanoTime() }
            DevDrawerGlanceWidget().update(context, glanceId)
        }

        suspend fun refreshAll(context: Context) {
            GlanceAppWidgetManager(context).getGlanceIds(DevDrawerGlanceWidget::class.java).forEach { refresh(context, it) }
        }
    }

    /** A widget whose setup isn't finished yet shows its empty state under the app's name. */
    private fun WidgetContent?.toUiState(context: Context, appWidgetId: Int): WidgetUiState {
        val apps = this?.apps.orEmpty()
        return WidgetUiState(
            appWidgetId = appWidgetId,
            title = this?.widget?.name ?: context.getString(R.string.app_name),
            headerColor = this?.widget?.headerColor ?: WidgetHeaderColor.AMBER,
            updatedAt = DateFormat.getTimeFormat(context).format(Date()),
            apps = apps.take(MAX_LISTED_APPS).map { it.toItem(context) },
            hiddenAppCount = (apps.size - MAX_LISTED_APPS).coerceAtLeast(0)
        )
    }

    private fun AppInfo.toItem(context: Context): WidgetAppItem = WidgetAppItem(
        name = name,
        packageName = packageName,
        icon = scaledIcon(context),
        canUninstall = canUninstall
    )

    private fun AppInfo.scaledIcon(context: Context): Bitmap? {
        val size = min((ICON_SIZE_DP * context.resources.displayMetrics.density).toInt(), MAX_ICON_SIZE_PX)
        return runCatching { appIcon.toBitmap(size, size) }.getOrNull()
    }
}
