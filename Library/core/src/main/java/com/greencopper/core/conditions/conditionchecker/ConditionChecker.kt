package com.greencopper.core.conditions.conditionchecker

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonElement

public interface ConditionChecker {

    public val metadata: MutableStateFlow<JsonElement>

    public fun check(conditionSet: ConditionSet): Boolean
    public fun check(conditionInfo: ConditionInfo): Boolean
    public fun checkFlow(conditionInfo: ConditionInfo): Flow<Boolean>
    public fun checkFlow(conditionSet: ConditionSet): Flow<Boolean>
}