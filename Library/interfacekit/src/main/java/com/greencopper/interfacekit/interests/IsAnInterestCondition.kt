package com.greencopper.interfacekit.interests

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class IsAnInterestCondition(private val lazyLocalStorage: LazyResolver<LocalStorage>) :
    ParameterizedCondition<IsAnInterestCondition.InterestData>() {

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("InterfaceKit.IsAnInterest", 1)
    }

    private val interestIdsLocalStorage: LocalStorageProperty<Set<String>>
        get() = lazyLocalStorage.resolve().project.interfaceKit.interestIds

    override fun checkWith(parameter: InterestData): Boolean =
        interestIdsLocalStorage.value.contains(parameter.interestId)

    override fun checkWithFlow(parameter: InterestData): Flow<Boolean> =
        interestIdsLocalStorage.state.map { it.contains(parameter.interestId) }

    override fun deserialize(conditionParameters: ConditionParameters): InterestData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    @Serializable
    internal data class InterestData(val interestId: String) : KiboSerializable<InterestData> {
        override fun getSerializer(): KSerializer<InterestData> = serializer()
    }
}
