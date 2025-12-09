package com.greencopper.core.conditions.conditionchecker

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.toolkit.di.binding.Creator
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.container.Key
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

internal interface ConditionResolver {
    fun resolve(key: ConditionInfo.Key, metadata: MutableStateFlow<JsonElement> = MutableStateFlow(JsonNull)): Condition?
}

public inline fun <reified T : Condition> Registrar.bindCondition(
    key: ConditionInfo.Key,
    noinline creator: Creator<T>
): Key = this.bindProvider<Condition>(key) { params ->
    creator(params)
}
