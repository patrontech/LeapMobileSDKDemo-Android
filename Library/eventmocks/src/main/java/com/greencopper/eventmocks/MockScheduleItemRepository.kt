package com.greencopper.eventmocks

import com.greencopper.event.scheduleItem.ScheduleItem
import com.greencopper.event.scheduleItem.data.repository.ScheduleItemRepository
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.filtering.QueryPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockScheduleItemRepository(public var returnData: List<ScheduleItem> = emptyList()) : ScheduleItemRepository {

    private var querySetup: FilteringPredicate.FilteringPredicateComputed? = null

    override suspend fun getScheduleItems(): Flow<List<ScheduleItem>> =
        flowOf(returnData)

    override suspend fun getScheduleItemById(scheduleItemId: Long): Flow<ScheduleItem?> =
        flowOf(returnData.find { it.itemId == scheduleItemId })

    override suspend fun getScheduleItemsForActivity(activityId: Long): Flow<List<ScheduleItem>> =
        flowOf(returnData.filter { it.activityId == activityId })

    override suspend fun getScheduleItemsForTags(query: QueryPattern?): Flow<List<ScheduleItem>> {
        if (querySetup?.toSQL() == query) {
            return querySetup?.toPredicate()?.let { predicate ->
                flowOf(returnData.filter { predicate.test(it.tags) })
            } ?: flowOf(returnData)
        } else {
            throw RuntimeException("Query wasn't setup")
        }
    }

    public fun setupQueryForTags(filteringPredicate: FilteringPredicate) {
        querySetup = filteringPredicate.query()
    }

    override suspend fun getScheduleItemsForPerformer(performerId: String): Flow<List<ScheduleItem>> =
        flowOf(returnData.filter { it.performerIds.contains(performerId) })
}
