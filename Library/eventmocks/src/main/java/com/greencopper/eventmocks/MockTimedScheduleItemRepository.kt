package com.greencopper.eventmocks

import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.data.repository.TimedScheduleItemRepository
import com.greencopper.interfacekit.filtering.QueryPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockTimedScheduleItemRepository(public var items: List<TimedScheduleItem> = emptyList()) :
    TimedScheduleItemRepository {

    public var getTimedScheduleItemsForActivityCalled: Boolean = false
        private set

    override suspend fun getTimedScheduleItemsForActivity(activityId: Long): Flow<List<TimedScheduleItem>> {
        getTimedScheduleItemsForActivityCalled = true
        return flowOf(items)
    }

    public var getTimedScheduleItemsForScheduleItemIdsCalled: Boolean = false
        private set

    override suspend fun getTimedScheduleItemsForScheduleItemIds(scheduleItemIds: List<Long>): Flow<List<TimedScheduleItem>> {
        getTimedScheduleItemsForScheduleItemIdsCalled = true
        return flowOf(items)
    }

    public var getTimedScheduleItemsForPerformerCalled: Boolean = false
        private set

    override suspend fun getTimedScheduleItemsForPerformer(performerId: String): Flow<List<TimedScheduleItem>> {
        getTimedScheduleItemsForPerformerCalled = true
        return flowOf(items)
    }

    public var getTimedScheduleItemsForTagsCalled: Boolean = false
        private set
    public var lastGetTimedScheduleItemsForTagsQueryPattern: QueryPattern? = null
    override suspend fun getTimedScheduleItemsForTags(query: QueryPattern?): Flow<List<TimedScheduleItem>> {
        getTimedScheduleItemsForTagsCalled = true
        lastGetTimedScheduleItemsForTagsQueryPattern = query
        return flowOf(items)
    }
}
