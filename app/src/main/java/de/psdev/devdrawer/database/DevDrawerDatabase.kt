package de.psdev.devdrawer.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Widget::class,
        WidgetProfile::class,
        PackageFilter::class
    ], version = DevDrawerDatabase.VERSION
)
@TypeConverters(Converters::class)
abstract class DevDrawerDatabase: RoomDatabase() {

    companion object {
        const val NAME = "DevDrawer.db"
        const val VERSION = 3
    }

    abstract fun widgetDao(): WidgetDao
    abstract fun packageFilterDao(): PackageFilterDao
    abstract fun widgetProfileDao(): WidgetProfileDao
}