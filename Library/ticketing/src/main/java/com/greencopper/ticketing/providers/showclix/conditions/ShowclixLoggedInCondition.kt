package com.greencopper.ticketing.providers.showclix.conditions

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.ticketing.providers.showclix.showclix
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class ShowclixLoggedInCondition(
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
) : ParameterizedCondition<ShowclixLoggedInCondition.ShowclixLoggedInConditionData>() {

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Ticketing.Showclix.LoggedIn", 1)
    }

    @Serializable
    internal data class ShowclixLoggedInConditionData(val isLoggedIn: Boolean) :
        KiboSerializable<ShowclixLoggedInConditionData> {
        override fun getSerializer(): KSerializer<ShowclixLoggedInConditionData> = serializer()
    }

    private val isLoggedInFlow =
        lazyLocalStorage.resolve().project.showclix.validationToken.state.map {
            !it.isNullOrBlank()
        }

    private val isLoggedIn get() = !lazyLocalStorage.resolve().project.showclix.validationToken.value.isNullOrBlank()

    override fun checkWith(parameter: ShowclixLoggedInConditionData): Boolean {
        val checkResult = parameter.isLoggedIn == isLoggedIn
        if (!checkResult) {
            lazyLocalStorage.resolve().project.showclix.timeToken.value = null
        }
        return checkResult
    }

    override fun checkWithFlow(parameter: ShowclixLoggedInConditionData): Flow<Boolean> =
        isLoggedInFlow.map {
            val checkResult = it == parameter.isLoggedIn
            if (!checkResult) {
                lazyLocalStorage.resolve().project.showclix.timeToken.value = null
            }
            checkResult
        }

    override fun deserialize(conditionParameters: ConditionParameters): ShowclixLoggedInConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)
}
