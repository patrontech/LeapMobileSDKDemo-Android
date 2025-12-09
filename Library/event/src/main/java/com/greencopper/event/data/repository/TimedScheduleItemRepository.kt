package com.greencopper.event.data.repository

import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.interfacekit.filtering.QueryPattern
import kotlinx.coroutines.flow.Flow

public interface TimedScheduleItemRepository {
    public suspend fun getTimedScheduleItemsForActivity(activityId: Long): Flow<List<TimedScheduleItem>>
    public suspend fun getTimedScheduleItemsForTags(query: QueryPattern?): Flow<List<TimedScheduleItem>>
    public suspend fun getTimedScheduleItemsForScheduleItemIds(scheduleItemIds: List<Long>): Flow<List<TimedScheduleItem>>
    public suspend fun getTimedScheduleItemsForPerformer(performerId: String): Flow<List<TimedScheduleItem>>
}
