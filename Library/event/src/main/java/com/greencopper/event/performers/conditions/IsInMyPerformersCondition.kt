package com.greencopper.event.performers.conditions

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.favorites.FavoritesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class IsInMyPerformersCondition(private val myPerformersManager: FavoritesManager<String>) :
    ParameterizedCondition<IsInMyPerformersCondition.MyPerformersData>() {

    override fun checkWith(parameter: MyPerformersData): Boolean =
        myPerformersManager.isInFavorites(parameter.performerId)

    override fun checkWithFlow(parameter: MyPerformersData): Flow<Boolean> =
        myPerformersManager.favoriteIdsFlow.map { it.contains(parameter.performerId) }

    override fun deserialize(conditionParameters: ConditionParameters): MyPerformersData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Event.IsInMyPerformers", 1)
    }

    @Serializable
    data class MyPerformersData(val performerId: String) : KiboSerializable<MyPerformersData> {
        override fun getSerializer(): KSerializer<MyPerformersData> = serializer()
    }
}
