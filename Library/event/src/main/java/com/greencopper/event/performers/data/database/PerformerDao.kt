package com.greencopper.event.performers.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
internal interface PerformerDao {

    @Insert
    fun insertAll(performers: List<PerformerEntity>)

    @Query("SELECT * FROM PerformerEntity")
    fun getAllPerformers(): List<PerformerEntity>

    @Query("SELECT * FROM PerformerEntity WHERE id LIKE :performerId")
    fun getPerformerById(performerId: String): PerformerEntity?

    @RawQuery(observedEntities = [PerformerEntity::class])
    fun getPerformersForTags(query: SupportSQLiteQuery): List<PerformerEntity>
}
