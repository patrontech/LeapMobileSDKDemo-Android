package com.greencopper.maps.common

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.favorites.FavoritesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class IsInMyLocationsCondition(private val myLocationsManager: FavoritesManager<String>) :
    ParameterizedCondition<IsInMyLocationsCondition.MyLocationData>() {

    override fun checkWith(parameter: MyLocationData): Boolean =
        myLocationsManager.isInFavorites(parameter.locationId)

    override fun checkWithFlow(parameter: MyLocationData): Flow<Boolean> =
        myLocationsManager.favoriteIdsFlow.map { it.contains(parameter.locationId) }

    override fun deserialize(conditionParameters: ConditionParameters): MyLocationData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Map.IsInMyLocations", 1)
    }

    @Serializable
    data class MyLocationData(val locationId: String) : KiboSerializable<MyLocationData> {
        override fun getSerializer(): KSerializer<MyLocationData> = serializer()
    }
}
