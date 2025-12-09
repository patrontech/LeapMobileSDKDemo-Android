package com.greencopper.event.data.database

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
internal interface TimedScheduleItemDao {

    @RawQuery
    fun getTimedScheduleItemsForTags(query: SupportSQLiteQuery): List<ScheduleItemTimeSlotStageJoined>

    @Transaction
    @Query(
        "SELECT ScheduleItemEntity.*" +
                " FROM TimeSlotEntity" +
                " INNER JOIN ScheduleItemEntity ON TimeSlotEntity.scheduleitemid = ScheduleItemEntity.id" +
                " LEFT JOIN StageEntity ON ScheduleItemEntity.stageId = StageEntity.id" +
                " WHERE activityId LIKE :activityId"
    )
    fun getTimedScheduleItemsForActivity(activityId: Long): List<ScheduleItemTimeSlotStageJoined>

    @Transaction
    @Query(
        "SELECT ScheduleItemEntity.*" +
                " FROM TimeSlotEntity" +
                " INNER JOIN ScheduleItemEntity ON TimeSlotEntity.scheduleitemid = ScheduleItemEntity.id" +
                " LEFT JOIN StageEntity ON ScheduleItemEntity.stageId = StageEntity.id" +
                " WHERE ScheduleItemEntity.id IN (:scheduleItemIds)"
    )
    fun getTimedScheduleItemsForScheduleIds(scheduleItemIds: List<Long>): List<ScheduleItemTimeSlotStageJoined>

    @Transaction
    @Query(
        "SELECT ScheduleItemEntity.*" +
                " FROM TimeSlotEntity" +
                " INNER JOIN ScheduleItemEntity ON TimeSlotEntity.scheduleitemid = ScheduleItemEntity.id" +
                " LEFT JOIN StageEntity ON ScheduleItemEntity.stageId = StageEntity.id" +
                " WHERE performerIds LIKE :performerId"
    )
    fun getTimedScheduleItemsForPerformer(performerId: String): List<ScheduleItemTimeSlotStageJoined>
}
