package com.greencopper.core.content.manager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.zip

public interface CurrentProjectTagProvider {
    public val currentProject: String?

    /**
     * Updates to contain the latest OTA project that was applied.
     * Remains empty if no new project has been applied.
     * */
    public val currentProjectFlow: Flow<String?>
}

public fun <T> Flow<T>.waitForContentApply(currentProjectTagProvider: CurrentProjectTagProvider): Flow<T> =
    zip(currentProjectTagProvider.currentProjectFlow.filterNotNull()) { value: T, _ ->
        value
    }
