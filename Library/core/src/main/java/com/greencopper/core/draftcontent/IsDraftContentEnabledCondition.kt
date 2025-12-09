package com.greencopper.core.draftcontent

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.conditionchecker.UnparameterizedCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class IsDraftContentEnabledCondition(
    private val draftContentManager: DraftContentManager,
) : UnparameterizedCondition() {

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Core.DraftContent.IsEnabled", 1)
    }

    override fun check(): Boolean = draftContentManager.passcode != null

    override fun checkFlow(): Flow<Boolean> = flowOf(draftContentManager.passcode != null)
}
