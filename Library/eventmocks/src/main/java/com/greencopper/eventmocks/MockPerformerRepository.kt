package com.greencopper.eventmocks

import com.greencopper.event.performers.Performer
import com.greencopper.event.performers.data.repository.PerformerRepository
import com.greencopper.interfacekit.filtering.QueryPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockPerformerRepository(
    public var performers: List<Performer> = emptyList(),
    public var performersWithPredicate: List<Performer> = emptyList(),
) : PerformerRepository {

    override fun getPerformers(): Flow<List<Performer>> = flowOf(performers)

    override fun getPerformerById(performerId: String): Flow<Performer?> =
        flowOf(performers.firstOrNull { it.itemId == performerId })

    override fun getPerformersForTags(query: QueryPattern?): Flow<List<Performer>> =
        flowOf(
            query?.let {
                performersWithPredicate
            } ?: performers
        )
}
