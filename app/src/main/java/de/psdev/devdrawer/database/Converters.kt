package de.psdev.devdrawer.database

import androidx.room.TypeConverter
import de.psdev.devdrawer.appwidget.SortOrder
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromFilterType(filterType: FilterType?): String? = filterType?.name

    @TypeConverter
    fun toFilterType(value: String?): FilterType? = value?.let { FilterType.valueOf(it) }

    @TypeConverter
    fun fromWidgetHeaderColor(value: WidgetHeaderColor?): String? = value?.name

    @TypeConverter
    fun toWidgetHeaderColor(value: String?): WidgetHeaderColor? = value?.let { WidgetHeaderColor.valueOf(it) }

    @TypeConverter
    fun fromSortOrder(value: SortOrder?): String? = value?.name

    @TypeConverter
    fun toSortOrder(value: String?): SortOrder? = value?.let { SortOrder.valueOf(it) }

    @TypeConverter
    fun fromOffsetDateTIme(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toOffsetDateTime(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }
}
