package com.greencopper.thuzi.account.registration

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

public class ThuziRegisteredCondition(
    private val localStorage: LocalStorage
) : ParameterizedCondition<ThuziRegisteredCondition.ThuziRegisteredConditionData>() {

    public companion object {
        public val key: ConditionInfo.Key = ConditionInfo.Key("Thuzi.Registration", 1)
    }

    private val isRegisteredFlow = localStorage.project.thuzi.registered.state

    private val isThuziRegistered
        get() = localStorage.project.thuzi.registered.value

    @Serializable
    public data class ThuziRegisteredConditionData(val isRegistered: Boolean) :
        KiboSerializable<ThuziRegisteredConditionData> {
        override fun getSerializer(): KSerializer<ThuziRegisteredConditionData> = serializer()
    }

    override fun checkWith(parameter: ThuziRegisteredConditionData): Boolean =
        parameter.isRegistered == isThuziRegistered

    override fun checkWithFlow(parameter: ThuziRegisteredConditionData): Flow<Boolean> =
        isRegisteredFlow.map { it == parameter.isRegistered }

    override fun deserialize(conditionParameters: ConditionParameters): ThuziRegisteredConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)
}
