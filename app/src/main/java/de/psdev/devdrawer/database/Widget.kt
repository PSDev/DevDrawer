package de.psdev.devdrawer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import de.psdev.devdrawer.appwidget.SortOrder

@Entity(
    tableName = "widgets",
    foreignKeys = [
        ForeignKey(
            entity = WidgetProfile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"]
        )
    ]
)
data class Widget(
    @PrimaryKey
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.INTEGER)
    val id: Int,
    @ColumnInfo(name = "name", index = true)
    var name: String,
    /** Legacy ARGB header colour from before [headerColor]; kept so the table needs no rebuild. */
    @ColumnInfo(name = "color", typeAffinity = ColumnInfo.INTEGER)
    var color: Int,
    @ColumnInfo(name = "profile_id", index = true)
    var profileId: String,
    @ColumnInfo(name = "header_color", defaultValue = "AMBER")
    val headerColor: WidgetHeaderColor = WidgetHeaderColor.AMBER,
    /** Sort order for this widget; null uses the default from Settings. */
    @ColumnInfo(name = "sort_order")
    val sortOrder: SortOrder? = null
)