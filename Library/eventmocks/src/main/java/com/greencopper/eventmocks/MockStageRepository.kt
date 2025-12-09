package com.greencopper.eventmocks

import com.greencopper.event.stage.Stage
import com.greencopper.event.stage.data.repository.StageRepository
import com.greencopper.interfacekit.filtering.QueryPattern
import kotlinx.coroutines.flow.*

public class MockStageRepository(
    public var stages: List<Stage> = emptyList(),
    public var stageFlowIsEmpty: Boolean = false,
): StageRepository {
    override suspend fun getStages(): Flow<List<Stage>> =
        if(stageFlowIsEmpty) {
            emptyFlow()
        } else {
            flowOf(stages)
        }

    override suspend fun getStageForId(id: Long): Flow<Stage?> =
        flowOf(stages.find { it.id == id })

    override suspend fun getStagesForTags(query: QueryPattern?): Flow<List<Stage>> =
        flowOf(stages)
}
