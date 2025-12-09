package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.json.JsonQueryParser
import com.greencopper.core.json.truthValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

public class MetadataQueryCondition : ParameterizedCondition<MetadataQueryCondition.MetadataQueryData>() {

    public var metadata: MutableStateFlow<JsonElement> = MutableStateFlow(JsonNull)

    override fun checkWith(parameter: MetadataQueryData): Boolean =
        check(parameter, metadata.value)

    override fun checkWithFlow(parameter: MetadataQueryData): Flow<Boolean> =
        metadata.map { check(parameter, it) }

    private fun check(params: MetadataQueryData, metadata: JsonElement): Boolean =
        JsonQueryParser.parse(params.query).eval(metadata).truthValue

    override fun deserialize(conditionParameters: ConditionParameters): MetadataQueryData
        = KiboSerializable.decodeFromJsonElement(conditionParameters)

    @Serializable
    public data class MetadataQueryData(val query: String) : KiboSerializable<MetadataQueryData> {
        override fun getSerializer(): KSerializer<MetadataQueryData> = serializer()
    }

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Core.MetadataQuery", 1)
    }
}
