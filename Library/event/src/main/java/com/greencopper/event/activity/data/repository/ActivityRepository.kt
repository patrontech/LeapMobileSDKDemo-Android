package com.greencopper.event.activity.data.repository

import com.greencopper.event.activity.ContentActivity
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.filtering.QueryPattern
import com.greencopper.interfacekit.lists.ListRepository
import kotlinx.coroutines.flow.Flow

public interface ActivityRepository : ListRepository<ContentActivity> {
    public suspend fun getActivities(): Flow<List<ContentActivity>>
    public suspend fun getActivityById(activityId: Long): Flow<ContentActivity?>
    public suspend fun getActivitiesForTags(query: QueryPattern?): Flow<List<ContentActivity>>

    override suspend fun getListData(predicate: FilteringPredicate?): Flow<List<ContentActivity>> =
        getActivitiesForTags(predicate?.query()?.toSQL())
}
