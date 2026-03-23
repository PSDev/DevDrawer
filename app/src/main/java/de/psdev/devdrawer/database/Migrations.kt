package de.psdev.devdrawer.database

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Color
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.psdev.devdrawer.appwidget.DDWidgetProvider
import java.util.UUID

class MigrationFrom1To2(
    private val application: Application
) : Migration(1, 2) {
    private val appWidgetManager by lazy { AppWidgetManager.getInstance(application) }

    override fun migrate(db: SupportSQLiteDatabase) {
        // Create profiles tables
        db.execSQL("CREATE TABLE IF NOT EXISTS `widget_profiles` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))")
        // Insert default profile
        val defaultProfileId = UUID.randomUUID().toString()
        db.execSQL("INSERT INTO `widget_profiles` (`id`, `name`) VALUES ('$defaultProfileId', 'Default')")

        // Migrate filters table
        db.execSQL("ALTER TABLE `filters` RENAME TO `filters_old`")
        db.execSQL("CREATE TABLE IF NOT EXISTS `filters` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `filter` TEXT NOT NULL, `description` TEXT NOT NULL, `profile_id` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`profile_id`) REFERENCES `widget_profiles`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
        db.execSQL("INSERT INTO `filters` (`id`, `type`, `filter`, `description`, `profile_id`) SELECT `id`, 'PACKAGE_NAME', `filter`, '', '$defaultProfileId' FROM `filters_old`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_filters_profile_id` ON `filters` (`profile_id`)")
        db.execSQL("DROP TABLE `filters_old`")

        // Create widgets table
        db.execSQL("CREATE TABLE IF NOT EXISTS `widgets` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `color` INTEGER NOT NULL, `profile_id` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`profile_id`) REFERENCES `widget_profiles`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_widgets_name` ON `widgets` (`name`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_widgets_profile_id` ON `widgets` (`profile_id`)")
        // Insert existing widgets
        val componentName = ComponentName(application, DDWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName).toList()
        for (appWidgetId in appWidgetIds) {
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES ($appWidgetId, 'Widget $appWidgetId', ${Color.BLACK}, '$defaultProfileId')")
        }
    }
}

object MigrationFrom2To3 : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `widget_profiles` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0;")
        db.execSQL("UPDATE `widget_profiles` SET `updatedAt` = ${System.currentTimeMillis()};")
    }
}
