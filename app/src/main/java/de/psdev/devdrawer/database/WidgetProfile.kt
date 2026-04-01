package de.psdev.devdrawer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(tableName = "widget_profiles")
data class WidgetProfile(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "name", typeAffinity = ColumnInfo.TEXT)
    var name: String,
    @ColumnInfo(name = "updatedAt")
    var updatedAt: Instant = Instant.now()
)