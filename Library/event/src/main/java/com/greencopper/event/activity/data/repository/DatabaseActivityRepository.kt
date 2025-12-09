package com.greencopper.event.activity.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.greencopper.event.activity.ContentActivity
import com.greencopper.event.activity.toDataModel
import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.interfacekit.filtering.QueryPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

internal class DatabaseActivityRepository(
    private val databaseHelper: DatabaseHelper,
    private val backgroundContext: CoroutineContext,
) : ActivityRepository {

    override suspend fun getActivities(): Flow<List<ContentActivity>> =
        databaseHelper.eventDatabase().map { db ->
            db.activityDao()
                .getAllActivities()
                .map {
                    it.toDataModel()
                }
        }.flowOn(backgroundContext)

    override suspend fun getActivityById(activityId: Long): Flow<ContentActivity?> =
        databaseHelper.eventDatabase().map { db ->
            db.activityDao()
                .getActivityById(activityId)
                ?.toDataModel()
        }.flowOn(backgroundContext)

    override suspend fun getActivitiesForTags(query: QueryPattern?): Flow<List<ContentActivity>> {
        val completedQuery = query?.let { "WHERE $query" } ?: ""
        val sqLiteQuery =
            SimpleSQLiteQuery("SELECT * FROM ContentActivityEntity $completedQuery")
        return databaseHelper.eventDatabase().map { db ->
            db.activityDao()
                .getActivitiesForTags(sqLiteQuery)
                .map {
                    it.toDataModel()
                }
        }.flowOn(backgroundContext)
    }
}
