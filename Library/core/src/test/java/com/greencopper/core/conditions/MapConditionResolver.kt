package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.Condition
import com.greencopper.core.conditions.conditionchecker.ConditionResolver
import com.greencopper.core.localstorage.LocalStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonElement

internal class MapConditionResolver: ConditionResolver {

    private val mapConditions: MutableMap<ConditionInfo.Key, Condition> = mutableMapOf()

    override fun resolve(key: ConditionInfo.Key, metadata: MutableStateFlow<JsonElement>): Condition? {
        val condition = mapConditions[key]
        (condition as? MetadataQueryCondition)?.metadata = metadata
        return condition
    }

    fun registerConditions(conditions:Set<Pair<ConditionInfo.Key, Condition>>) {
        conditions.forEach {
            mapConditions[it.first] = it.second
        }
    }

    companion object {
        internal fun createTestResolver(localStorage: LocalStorage): MapConditionResolver {
            return MapConditionResolver().also {
                it.registerConditions(setOf(
                    Pair(TestCondition.key, TestCondition()),
                    Pair(TestUpdatingCondition.key, TestUpdatingCondition(localStorage))
                ))
            }
        }
    }

}
