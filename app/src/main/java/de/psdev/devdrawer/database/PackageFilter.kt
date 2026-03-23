package de.psdev.devdrawer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import de.psdev.devdrawer.appwidget.PackageHashInfo
import java.util.UUID

@Entity(
    tableName = "filters",
    foreignKeys = [
        ForeignKey(
            entity = WidgetProfile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class PackageFilter(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "type")
    val type: FilterType = FilterType.PACKAGE_NAME,
    @ColumnInfo(name = "filter")
    val filter: String,
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "profile_id", index = true)
    val profileId: String
) {
    @delegate:Ignore
    private val filterRegex: Regex by lazy { filter.replace("*", ".*").toRegex() }

    fun matches(packageHashInfo: PackageHashInfo): Boolean = when (type) {
        FilterType.PACKAGE_NAME -> filterRegex.matches(packageHashInfo.packageName)
        FilterType.SIGNATURE -> filter == packageHashInfo.signatureHashSha256
    }

}

