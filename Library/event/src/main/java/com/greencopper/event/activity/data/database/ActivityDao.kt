package com.greencopper.event.activity.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
internal interface ActivityDao {

    @Query("SELECT * FROM ContentActivityEntity")
    fun getAllActivities(): List<ContentActivityEntity>

    @Insert
    fun insertAll(contentActivities: List<ContentActivityEntity>)

    @Query("SELECT * FROM ContentActivityEntity WHERE id LIKE :activityId")
    fun getActivityById(activityId: Long): ContentActivityEntity?

    @RawQuery(observedEntities = [ContentActivityEntity::class])
    fun getActivitiesForTags(query: SupportSQLiteQuery): List<ContentActivityEntity>
}