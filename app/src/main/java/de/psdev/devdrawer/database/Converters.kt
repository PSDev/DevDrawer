package de.psdev.devdrawer.database

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromFilterType(filterType: FilterType?): String? = filterType?.name

    @TypeConverter
    fun toFilterType(value: String?): FilterType? = value?.let { FilterType.valueOf(it) }

    @TypeConverter
    fun fromOffsetDateTIme(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toOffsetDateTime(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }
}
