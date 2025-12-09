package com.greencopper.event.stage.data.repository

import com.greencopper.event.stage.Stage
import com.greencopper.interfacekit.filtering.QueryPattern
import kotlinx.coroutines.flow.Flow

public interface StageRepository {
    public suspend fun getStages(): Flow<List<Stage>>

    public suspend fun getStageForId(id: Long): Flow<Stage?>

    public suspend fun getStagesForTags(query: QueryPattern?): Flow<List<Stage>>
}