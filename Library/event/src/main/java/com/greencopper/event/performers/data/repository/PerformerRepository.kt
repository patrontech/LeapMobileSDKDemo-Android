package com.greencopper.event.performers.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.greencopper.event.dao.DatabaseHelper
import com.greencopper.event.performers.Performer
import com.greencopper.event.performers.toDataModel
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.filtering.QueryPattern
import com.greencopper.interfacekit.lists.ListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

public interface PerformerRepository : ListRepository<Performer> {
    public fun getPerformers(): Flow<List<Performer>>
    public fun getPerformerById(performerId: String): Flow<Performer?>
    public fun getPerformersForTags(query: QueryPattern?): Flow<List<Performer>>

    override suspend fun getListData(predicate: FilteringPredicate?): Flow<List<Performer>> =
        getPerformersForTags(predicate?.query()?.toSQL())
}

internal class DatabasePerformerRepository(
    private val databaseHelper: DatabaseHelper,
    private val backgroundContext: CoroutineContext,
) : PerformerRepository {

    override fun getPerformers(): Flow<List<Performer>> =
        databaseHelper.eventDatabase().map { db ->
            db.performerDao()
                .getAllPerformers()
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)

    override fun getPerformerById(performerId: String): Flow<Performer?> =
        databaseHelper.eventDatabase().map {
            it.performerDao()
                .getPerformerById(performerId)
                ?.toDataModel()
        }.flowOn(backgroundContext)

    override fun getPerformersForTags(query: QueryPattern?): Flow<List<Performer>> {
        val completedQuery = query?.let { "WHERE $query" } ?: ""
        val sqLiteQuery = SimpleSQLiteQuery("SELECT * FROM PerformerEntity $completedQuery")
        return databaseHelper.eventDatabase().map { db ->
            db.performerDao()
                .getPerformersForTags(sqLiteQuery)
                .map { it.toDataModel() }
        }.flowOn(backgroundContext)
    }
}
