package com.greencopper.event.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.data.toDataModel
import com.greencopper.interfacekit.filtering.QueryPattern
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

internal class DatabaseTimedScheduleItemRepository(
    private val databaseHelper: DatabaseHelper,
    private val backgroundContext: CoroutineContext,
) : TimedScheduleItemRepository {

    override suspend fun getTimedScheduleItemsForActivity(activityId: Long): Flow<List<TimedScheduleItem>> =
        databaseHelper.eventDatabase().map { db ->
            db.timedScheduleItemDao()
                .getTimedScheduleItemsForActivity(activityId)
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)

    override suspend fun getTimedScheduleItemsForTags(query: QueryPattern?): Flow<List<TimedScheduleItem>> {
        val completedQuery = query
            ?.replace("tags", "ScheduleItemEntity.tags")
            ?.let { " WHERE $it" }
            ?: ""
        val sqLiteQuery = SimpleSQLiteQuery("$requestPrefix$completedQuery")
        return databaseHelper.eventDatabase().map { db ->
            db.timedScheduleItemDao()
                .getTimedScheduleItemsForTags(sqLiteQuery)
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)
    }

    override suspend fun getTimedScheduleItemsForScheduleItemIds(scheduleItemIds: List<Long>): Flow<List<TimedScheduleItem>> =
        databaseHelper.eventDatabase().map {
            it.timedScheduleItemDao()
                .getTimedScheduleItemsForScheduleIds(scheduleItemIds)
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)

    override suspend fun getTimedScheduleItemsForPerformer(performerId: String): Flow<List<TimedScheduleItem>> =
        databaseHelper.eventDatabase().map {
            it.timedScheduleItemDao()
                .getTimedScheduleItemsForPerformer("%\"$performerId\"%")
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)

    internal companion object {
        internal const val requestPrefix: String =
            "SELECT ScheduleItemEntity.*" +
                    " FROM TimeSlotEntity" +
                    " INNER JOIN ScheduleItemEntity ON TimeSlotEntity.scheduleitemid = ScheduleItemEntity.id" +
                    " LEFT JOIN StageEntity ON ScheduleItemEntity.stageId = StageEntity.id"
    }
}
