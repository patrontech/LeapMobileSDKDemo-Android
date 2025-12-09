package com.greencopper.event.scheduleItem.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
internal interface ScheduleItemDao {

    @Query("SELECT * FROM ScheduleItemEntity")
    fun getAllScheduleItems(): List<ScheduleItemEntity>

    @Query("SELECT * FROM ScheduleItemEntity WHERE activityId LIKE :activityId")
    fun getScheduleItemsForActivity(activityId: Long): List<ScheduleItemEntity>

    @Query("SELECT * FROM ScheduleItemEntity WHERE id LIKE :scheduleItemId")
    fun getScheduleItemById(scheduleItemId: Long): ScheduleItemEntity?

    @RawQuery(observedEntities = [ScheduleItemEntity::class])
    fun getScheduleItemsForTags(query: SupportSQLiteQuery): List<ScheduleItemEntity>

    @Query("SELECT * FROM ScheduleItemEntity WHERE performerIds LIKE :performerId")
    fun getScheduleItemsForPerformer(performerId: String): List<ScheduleItemEntity>

    @Insert
    fun insertAll(scheduleItems: List<ScheduleItemEntity>)
}
