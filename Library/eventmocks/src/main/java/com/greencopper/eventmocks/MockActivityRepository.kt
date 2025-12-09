package com.greencopper.eventmocks

import com.greencopper.event.activity.ContentActivity
import com.greencopper.event.activity.data.repository.ActivityRepository
import com.greencopper.interfacekit.filtering.QueryPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockActivityRepository(
    public var activities: List<ContentActivity> = emptyList(),
    public var activitiesWithPredicate: List<ContentActivity> = emptyList(),
): ActivityRepository {
    override suspend fun getActivities(): Flow<List<ContentActivity>> {
        return flowOf(activities)
    }

    public var lastGetActivityByIdArg: Long? = null
    override suspend fun getActivityById(activityId: Long): Flow<ContentActivity?> {
        lastGetActivityByIdArg = activityId
        return flowOf(activities.find { it.itemId == activityId })
    }

    override suspend fun getActivitiesForTags(query: QueryPattern?): Flow<List<ContentActivity>> =
        flowOf(query?.let {
            activitiesWithPredicate
        } ?: activities)

}
