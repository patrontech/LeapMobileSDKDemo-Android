package com.greencopper.core.services.iplocation

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class IPLocationRestrictedAreaCondition(
    private val localStorage: LocalStorage,
    private val iplocationService: IPLocationService,
): ParameterizedCondition<IPLocationRestrictedAreaCondition.IPLocationRestrictedAreaConditionData>() {
    internal companion object {
        internal val key = ConditionInfo.Key("Core.IPLocation.RestrictedArea", 1)
    }
    @Serializable
    internal data class IPLocationRestrictedAreaConditionData(
        val location: RestrictedArea
    ): KiboSerializable<IPLocationRestrictedAreaConditionData> {
        override fun getSerializer(): KSerializer<IPLocationRestrictedAreaConditionData> =
            serializer()
    }

    override fun checkWith(parameter: IPLocationRestrictedAreaConditionData): Boolean {
        /*
         When iplocation is not null, it means that the
         `IPLocationService` has already run and there's no
         need to do the complex dance in the code
         below. This will be the most common case. The
         complex code will only run the very
         first time the app launches.
         */
        if (localStorage.app.core.iplocation.value != null) {
            return checkParameter(localStorage.app.core.iplocation.value, parameter)
        }

        /*
         Unfortunately we have little choice about this.
         The QA team in Vietnam is seeing a race condition. What's
         happening is that as soon as the `currentContentFlow`
         "fires", there's a race between the UI and the `IPLocationService`.
         In North America, the `IPLocationService` wins. In Vietnam,
         the UI wins. This is without question because of latency.
         */
        runBlocking {
            // We can still have a race condition if the `API_TIMEOUT` isn't high enough.
            // In that case, the UI will fall back to `IN_RESTRICTED_AREA` and OneTrust
            // will be enabled. This is by design.
            withTimeoutOrNull(ConcreteIPLocationService.API_TIMEOUT.toMillis() + 100L) {
                iplocationService.completedFlow.filter { it }.first()
            }
        }

        return checkParameter(localStorage.app.core.iplocation.value, parameter)
    }

    override fun checkWithFlow(parameter: IPLocationRestrictedAreaConditionData): Flow<Boolean> =
        iplocationService.completedFlow
            .filter { it }
            .combine(localStorage.app.core.iplocation.state) { _, iplocation ->
                checkParameter(iplocation, parameter)
            }

    override fun deserialize(conditionParameters: ConditionParameters): IPLocationRestrictedAreaConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    private fun checkParameter(
        iplocation: IPLocation?,
        parameter: IPLocationRestrictedAreaConditionData
    ): Boolean =
        (iplocation?.location ?: RestrictedArea.IN_RESTRICTED_AREA) == parameter.location
}
