package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class PlatformCondition : ParameterizedCondition<PlatformCondition.PlatformData>() {

    override fun checkWith(parameter: PlatformData): Boolean = parameter.platform.equals("android", ignoreCase = true)

    override fun checkWithFlow(parameter: PlatformData): Flow<Boolean> =
        flowOf(checkWith(parameter))

    override fun deserialize(conditionParameters: ConditionParameters): PlatformData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    @Serializable
    internal data class PlatformData(val platform: String) : KiboSerializable<PlatformData> {
        override fun getSerializer(): KSerializer<PlatformData> = serializer()
    }

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Core.Platform", 1)
    }
}
