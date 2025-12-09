package com.greencopper.event.stage.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.stage.Stage
import com.greencopper.event.stage.toDataModel
import com.greencopper.interfacekit.filtering.QueryPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

internal class DatabaseStageRepository(
    private val databaseHelper: DatabaseHelper,
    private val backgroundContext: CoroutineContext,
) : StageRepository {

    override suspend fun getStages(): Flow<List<Stage>> =
        databaseHelper.eventDatabase().map { db ->
            db.stageDao()
                .getAllStages()
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)

    override suspend fun getStageForId(id: Long): Flow<Stage?> =
        databaseHelper.eventDatabase().map {
            it.stageDao()
                .getStageForId(id)
                ?.toDataModel()
        }.flowOn(backgroundContext)

    override suspend fun getStagesForTags(query: QueryPattern?): Flow<List<Stage>> {
        val completedQuery = query?.let { "WHERE $query" } ?: ""
        val sqLiteQuery = SimpleSQLiteQuery("SELECT * FROM StageEntity $completedQuery")
        return databaseHelper.eventDatabase().map { db ->
            db.stageDao()
                .getStagesForTags(sqLiteQuery)
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)
    }
}
