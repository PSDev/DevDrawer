package de.psdev.devdrawer.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WidgetProfileDao : BaseDao<WidgetProfile>() {
    @Query("SELECT * FROM widget_profiles")
    abstract suspend fun findAll(): List<WidgetProfile>

    @Query("SELECT * FROM widget_profiles")
    abstract fun findAllFlow(): Flow<List<WidgetProfile>>

    @Query("SELECT * FROM widget_profiles WHERE id = :id")
    abstract suspend fun findById(id: String): WidgetProfile?

    @Transaction
    @Query("SELECT * FROM widget_profiles WHERE id = :id")
    abstract fun widgetProfileWithIdObservable(id: String): Flow<WidgetProfile>

}
