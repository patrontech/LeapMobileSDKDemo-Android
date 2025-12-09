package com.greencopper.core.localstorage

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.json.JsonQueryParser
import com.greencopper.core.json.truthValue
import com.greencopper.core.localstorage.LocalStorageQueryCondition.LocalStorageQueryConditionData
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

internal class LocalStorageQueryCondition(private val lazyLocalStorage: LazyResolver<LocalStorage>) :
    ParameterizedCondition<LocalStorageQueryConditionData>() {
    internal companion object {
        internal val key = ConditionInfo.Key("Core.LocalStorageQuery", 1)
    }

    @Serializable
    internal data class LocalStorageQueryConditionData(
        val key: String,
        val query: String,
    ) : KiboSerializable<LocalStorageQueryConditionData> {
        override fun getSerializer(): KSerializer<LocalStorageQueryConditionData> =
            serializer()
    }

    override fun checkWith(parameter: LocalStorageQueryConditionData): Boolean {
        val localStorage = lazyLocalStorage.resolve()
        val key =
            LocalStorageKey(parameter.key).inProject(localStorage.project.localStorageDomainName.toString())
        val json: JsonElement = localStorage.localStorageContainer.getJSON(key)?.let {
            KiboSerializable.decodeFromString(it)
        } ?: JsonNull
        return JsonQueryParser.parse(parameter.query).eval(json).truthValue
    }

    override fun checkWithFlow(parameter: LocalStorageQueryConditionData): Flow<Boolean> =
        flowOf(checkWith(parameter))

    override fun deserialize(conditionParameters: ConditionParameters): LocalStorageQueryConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)
}
