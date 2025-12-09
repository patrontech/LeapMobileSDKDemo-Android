package com.greencopper.testmocks.core

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionSet
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

public class MockConditionChecker(
    public var mockMetadata: JsonElement = JsonNull,
    public var mockCheckConditionSet: (ConditionSet) -> Boolean = { true },
    public var mockCheckConditionInfo: (ConditionInfo) -> Boolean = { true },
    public var mockCheckConditionInfoFlow: (ConditionInfo) -> Flow<Boolean> = { flowOf(true) },
    public var mockCheckConditionSetFlow: (ConditionSet) -> Flow<Boolean> = { flowOf(true) },
): ConditionChecker {
    override val metadata: MutableStateFlow<JsonElement>
        get() = MutableStateFlow(mockMetadata)

    override fun check(conditionSet: ConditionSet): Boolean = mockCheckConditionSet(conditionSet)

    override fun check(conditionInfo: ConditionInfo): Boolean = mockCheckConditionInfo(conditionInfo)

    override fun checkFlow(conditionInfo: ConditionInfo): Flow<Boolean> = mockCheckConditionInfoFlow(conditionInfo)

    override fun checkFlow(conditionSet: ConditionSet): Flow<Boolean> = mockCheckConditionSetFlow(conditionSet)
}
