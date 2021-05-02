package de.psdev.devdrawer.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PackageFilterDao : BaseDao<PackageFilter>() {

    @Query("SELECT * FROM filters WHERE id = :id")
    abstract suspend fun findById(id: String): PackageFilter?

    @Query("SELECT * FROM filters WHERE profile_id = :profileId")
    abstract suspend fun findAllByProfile(profileId: String): List<PackageFilter>

    @Query("SELECT * FROM filters WHERE profile_id = :profileId")
    abstract fun findAllByProfileFlow(profileId: String): Flow<List<PackageFilter>>

    @Query("DELETE FROM filters WHERE id = :id")
    abstract suspend fun deleteById(id: String)

}