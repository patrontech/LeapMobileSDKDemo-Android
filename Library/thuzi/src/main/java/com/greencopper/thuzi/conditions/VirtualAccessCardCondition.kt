package com.greencopper.thuzi.conditions

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.thuzi.localstorage.thuzi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class VirtualAccessCardCondition(
    private val localStorage: LocalStorage,
) : ParameterizedCondition<VirtualAccessCardCondition.VirtualAccessCardConditionData>() {

    override fun checkWith(parameter: VirtualAccessCardConditionData): Boolean =
        localStorage.project.thuzi.state.value.virtualAccessCards?.contains(parameter.virtualAccessCard) == true

    override fun checkWithFlow(parameter: VirtualAccessCardConditionData): Flow<Boolean> =
        localStorage.project.thuzi.state.state.map { it.virtualAccessCards?.contains(parameter.virtualAccessCard) == true }

    override fun deserialize(conditionParameters: ConditionParameters): VirtualAccessCardConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    internal companion object {
        internal val key = ConditionInfo.Key("Thuzi.VirtualAccessCard", 1)
    }

    @Serializable
    internal data class VirtualAccessCardConditionData(
        val virtualAccessCard: String,
    ): KiboSerializable<VirtualAccessCardConditionData> {
        override fun getSerializer(): KSerializer<VirtualAccessCardConditionData> = serializer()
    }
}