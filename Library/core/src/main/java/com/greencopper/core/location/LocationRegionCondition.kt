package com.greencopper.core.location

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.core
import com.greencopper.core.location.LocationRegionCondition.LocationRegionConditionData
import com.greencopper.core.location.localstorage.location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class LocationRegionCondition(
    localStorage: LocalStorage
) : ParameterizedCondition<LocationRegionConditionData>() {

    private val currentRegions: LocalStorageProperty<Set<Int>> =
        localStorage.app.core.location.currentRegions

    @Serializable
    internal data class LocationRegionConditionData(
        @SerialName("region") val regionId: Int,
        @SerialName("inside") val isInside: Boolean) :
        KiboSerializable<LocationRegionConditionData> {
        override fun getSerializer(): KSerializer<LocationRegionConditionData> = serializer()
    }

    private fun isValid(parameter: LocationRegionConditionData, currentRegions: Set<Int>): Boolean {
        val insideCurrentRegions = currentRegions.any { it == parameter.regionId }
        return parameter.isInside == insideCurrentRegions
    }

    override fun checkWith(parameter: LocationRegionConditionData): Boolean {
        return isValid(parameter, currentRegions.value)
    }

    override fun checkWithFlow(parameter: LocationRegionConditionData): Flow<Boolean> {
        return currentRegions.state.map { set ->
            isValid(parameter, set)
        }
    }

    override fun deserialize(conditionParameters: ConditionParameters): LocationRegionConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Core.Location.Region", 1)
    }
}
