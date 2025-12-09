package com.greencopper.event.scheduleItem.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.scheduleItem.ScheduleItem
import com.greencopper.event.scheduleItem.toDataModel
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.filtering.QueryPattern
import com.greencopper.interfacekit.lists.ListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

public interface ScheduleItemRepository : ListRepository<ScheduleItem> {
    public suspend fun getScheduleItems(): Flow<List<ScheduleItem>>
    public suspend fun getScheduleItemById(scheduleItemId: Long): Flow<ScheduleItem?>
    public suspend fun getScheduleItemsForActivity(activityId: Long): Flow<List<ScheduleItem>>
    public suspend fun getScheduleItemsForTags(query: QueryPattern?): Flow<List<ScheduleItem>>
    public suspend fun getScheduleItemsForPerformer(performerId: String): Flow<List<ScheduleItem>>

    override suspend fun getListData(predicate: FilteringPredicate?): Flow<List<ScheduleItem>> =
        getScheduleItemsForTags(predicate?.query()?.toSQL())
}

internal class DatabaseScheduleItemRepository(
    private val databaseHelper: DatabaseHelper,
    private val backgroundContext: CoroutineContext,
) : ScheduleItemRepository {

    override suspend fun getScheduleItems(): Flow<List<ScheduleItem>> =
        databaseHelper.eventDatabase().map { db ->
            db.scheduleItemDao()
                .getAllScheduleItems()
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)

    override suspend fun getScheduleItemById(scheduleItemId: Long): Flow<ScheduleItem?> =
        databaseHelper.eventDatabase().map {
            it.scheduleItemDao()
                .getScheduleItemById(scheduleItemId)
                ?.toDataModel()
        }.flowOn(backgroundContext)

    override suspend fun getScheduleItemsForActivity(activityId: Long): Flow<List<ScheduleItem>> =
        databaseHelper.eventDatabase().map { db ->
            db.scheduleItemDao()
                .getScheduleItemsForActivity(activityId)
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)

    override suspend fun getScheduleItemsForTags(query: QueryPattern?): Flow<List<ScheduleItem>> {
        val completedQuery = query?.let { "WHERE $query" } ?: ""
        val sqLiteQuery = SimpleSQLiteQuery("SELECT * FROM ScheduleItemEntity $completedQuery")
        return databaseHelper.eventDatabase().map { db ->
            db.scheduleItemDao()
                .getScheduleItemsForTags(sqLiteQuery)
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)
    }

    override suspend fun getScheduleItemsForPerformer(performerId: String): Flow<List<ScheduleItem>> =
        databaseHelper.eventDatabase().map { db ->
            db.scheduleItemDao()
                .getScheduleItemsForPerformer("%\"$performerId\"%")
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)
}
