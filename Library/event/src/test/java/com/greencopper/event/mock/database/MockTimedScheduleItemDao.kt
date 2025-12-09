package com.greencopper.event.mock.database

import androidx.sqlite.db.SupportSQLiteQuery
import com.greencopper.event.data.database.ScheduleItemTimeSlotStageJoined
import com.greencopper.event.data.database.TimedScheduleItemDao

internal class MockTimedScheduleItemDao : TimedScheduleItemDao {

    var getTimedScheduleItemsForActivityResult: (activityId: Long) -> List<ScheduleItemTimeSlotStageJoined> =
        { emptyList() }

    override fun getTimedScheduleItemsForActivity(activityId: Long): List<ScheduleItemTimeSlotStageJoined> =
        getTimedScheduleItemsForActivityResult(activityId)

    var getTimedScheduleItemsForTagsResult: (query: SupportSQLiteQuery) -> List<ScheduleItemTimeSlotStageJoined> =
        { emptyList() }

    override fun getTimedScheduleItemsForTags(query: SupportSQLiteQuery): List<ScheduleItemTimeSlotStageJoined> =
        getTimedScheduleItemsForTagsResult(query)

    var getTimedScheduleItemsForScheduleIdsResult: (scheduleItemIds: List<Long>) -> List<ScheduleItemTimeSlotStageJoined> =
        { emptyList() }

    override fun getTimedScheduleItemsForScheduleIds(scheduleItemIds: List<Long>): List<ScheduleItemTimeSlotStageJoined> =
        getTimedScheduleItemsForScheduleIdsResult(scheduleItemIds)

    var getTimedScheduleItemsForPerformerResult: (performerId: String) -> List<ScheduleItemTimeSlotStageJoined> =
        { emptyList() }

    override fun getTimedScheduleItemsForPerformer(performerId: String): List<ScheduleItemTimeSlotStageJoined> =
        getTimedScheduleItemsForPerformerResult(performerId)

}
