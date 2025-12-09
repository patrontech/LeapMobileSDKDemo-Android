package com.greencopper.event.activity.conditions

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.favorites.FavoritesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class IsInMyActivitiesCondition(private val myActivitiesManager: FavoritesManager<Long>) :
    ParameterizedCondition<IsInMyActivitiesCondition.MyActivityData>() {

    override fun checkWith(parameter: MyActivityData): Boolean =
        myActivitiesManager.isInFavorites(parameter.activityId)

    override fun checkWithFlow(parameter: MyActivityData): Flow<Boolean> =
        myActivitiesManager.favoriteIdsFlow.map { it.contains(parameter.activityId) }

    override fun deserialize(conditionParameters: ConditionParameters): MyActivityData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Event.IsInMyActivities", 1)
    }

    @Serializable
    data class MyActivityData(val activityId: Long) : KiboSerializable<MyActivityData> {
        override fun getSerializer(): KSerializer<MyActivityData> = serializer()
    }
}
