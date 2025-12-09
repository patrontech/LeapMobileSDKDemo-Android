package com.greencopper.event.stage.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
internal interface StageDao {

    @Query("SELECT * FROM StageEntity")
    fun getAllStages(): List<StageEntity>

    @Insert
    fun insertAll(stages: List<StageEntity>)

    @Query("SELECT * FROM StageEntity WHERE id LIKE :stageId")
    fun getStageForId(stageId: Long): StageEntity?

    @RawQuery(observedEntities = [StageEntity::class])
    fun getStagesForTags(query: SupportSQLiteQuery): List<StageEntity>
}
