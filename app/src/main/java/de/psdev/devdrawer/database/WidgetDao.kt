package de.psdev.devdrawer.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WidgetDao: BaseDao<Widget>() {

    @Query("SELECT * FROM widgets")
    abstract suspend fun findAll(): List<Widget>

    @Query("SELECT * FROM widgets")
    abstract fun findAllBlocking(): List<Widget>

    @Query("SELECT * FROM widgets")
    abstract fun findAllFlow(): Flow<List<Widget>>

    @Query("SELECT * FROM widgets WHERE profile_id = :profileId")
    abstract suspend fun findAllByProfileId(profileId: String): List<Widget>

    @Query("SELECT * FROM widgets WHERE id = :id")
    abstract suspend fun findById(id: Int): Widget?

    @Transaction
    @Query("SELECT * FROM widgets WHERE id = :id")
    abstract fun widgetWithIdObservable(id: Int): Flow<Widget>

    @Query("DELETE FROM widgets WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>)

}
