package de.psdev.devdrawer.appwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.StringRes
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.utils.Constants
import kotlinx.coroutines.runBlocking
import mu.KLogging

class WidgetAppsListViewFactory(
    private val context: Context,
    private val devDrawerDatabase: DevDrawerDatabase,
    private val sharedPreferences: SharedPreferences,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    companion object : KLogging()

    private val appWidgetId: Int by lazy {
        intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }

    private val packageManager: PackageManager by lazy { context.packageManager }

    private val apps: MutableList<AppInfo> = mutableListOf()

    // ==========================================================================================================================
    // RemoteViewsService.RemoteViewsFactory
    // ==========================================================================================================================

    override fun onCreate() {
        logger.warn { "onCreate" }
    }

    override fun onDataSetChanged() {
        logger.warn { "onDataSetChanged" }
        runBlocking {
            loadApps()
        }
    }

    override fun onDestroy() {
        logger.warn { "onDestroy" }
    }

    override fun getCount(): Int = apps.size

    override fun getViewAt(position: Int): RemoteViews? {
        logger.debug { "getViewAt[position=$position]" }
        val (appName, packageName, appIcon) = apps[position]

        // Setup the list item and intents for on click
        val row = RemoteViews(context.packageName, R.layout.list_item)

        try {
            row.setTextViewText(R.id.packageNameTextView, packageName)
            row.setTextViewText(R.id.appNameTextView, appName)
            row.setImageViewBitmap(R.id.imageView, convertFromDrawable(appIcon))

            val appDetailsClickIntent = Intent().apply {
                putExtra(ClickHandlingActivity.EXTRA_LAUNCH_TYPE, Constants.LAUNCH_APP_DETAILS)
                putExtra(ClickHandlingActivity.EXTRA_PACKAGE_NAME, packageName)
            }
            row.setOnClickFillInIntent(R.id.appDetailsImageButton, appDetailsClickIntent)

            val uninstallClickIntent = Intent().apply {
                putExtra(ClickHandlingActivity.EXTRA_LAUNCH_TYPE, Constants.LAUNCH_UNINSTALL)
                putExtra(ClickHandlingActivity.EXTRA_PACKAGE_NAME, packageName)
            }
            row.setOnClickFillInIntent(R.id.uninstallImageButton, uninstallClickIntent)

            val rowClickIntent = Intent().apply {
                putExtra(ClickHandlingActivity.EXTRA_LAUNCH_TYPE, Constants.LAUNCH_APP)
                putExtra(ClickHandlingActivity.EXTRA_PACKAGE_NAME, packageName)
            }
            row.setOnClickFillInIntent(R.id.container_row, rowClickIntent)

            return row
        } catch (e: IndexOutOfBoundsException) {
            logger.warn(e) { "Error: ${e.message}" }
            return null
        }

    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = apps[position].packageName.hashCode().toLong()

    override fun hasStableIds(): Boolean = true

    // ==========================================================================================================================
    // Private API
    // ==========================================================================================================================

    /**
     * Method to get all apps from the app database and add to the dataset
     */
    @Suppress("DEPRECATION")
    private suspend fun loadApps() {
        val devDrawerDatabase = devDrawerDatabase
        val widget =
            devDrawerDatabase.widgetDao().findById(appWidgetId) ?: throw IllegalStateException("Unknown widget")

        val packageFilters = devDrawerDatabase.packageFilterDao()
            .findAllByProfile(widget.profileId)

        val installedPackages =
            packageManager.getInstalledPackages(PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES)
                .map { it.toPackageHashInfo() }
        val appList = installedPackages.asSequence()
            .filter { packageInfo ->
                packageFilters.any { filter -> filter.matches(packageInfo) }
            }
            .mapNotNull { it.toAppInfo(context) }
            .sortedWith(appComparator)
            .distinct()
            .toList()

        apps.clear()
        apps.addAll(appList)
    }

    private val appComparator: Comparator<AppInfo>
        get() {
            val defaultSortOrder = context.getString(R.string.pref_sort_order_default)
            return when (SortOrder.valueOf(
                sharedPreferences.getNonNullString(
                    R.string.pref_sort_order,
                    defaultSortOrder
                )
            )) {
                SortOrder.FIRST_INSTALLED -> compareByDescending { it.firstInstallTime }
                SortOrder.LAST_UPDATED -> compareByDescending { it.lastUpdateTime }
                SortOrder.NAME -> compareBy { it.name }
                SortOrder.PACKAGE_NAME -> compareBy { it.packageName }
            }
        }

    /**
     * Method to return a bitmap from drawable
     */
    private fun convertFromDrawable(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            getBitmapFromDrawable(drawable)
        }
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    private fun SharedPreferences.getNonNullString(
        @StringRes stringRes: Int,
        defaultValue: String
    ): String = getString(context.getString(stringRes), defaultValue) ?: defaultValue

}
