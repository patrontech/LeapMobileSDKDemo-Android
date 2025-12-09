package com.greencopper.event.scheduleItem.conditions

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.favorites.FavoritesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class IsInMyScheduleCondition(
    private val manager: FavoritesManager<Long>,
) : ParameterizedCondition<IsInMyScheduleCondition.MyScheduleData>() {

    override fun checkWith(parameter: MyScheduleData): Boolean =
        manager.favoriteIds.contains(parameter.scheduleItemId)

    override fun checkWithFlow(parameter: MyScheduleData): Flow<Boolean> =
        manager.favoriteIdsFlow.map { items ->
            items.contains(parameter.scheduleItemId)
        }

    override fun deserialize(conditionParameters: ConditionParameters): MyScheduleData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Event.IsInMySchedule", 1)
    }

    @Serializable
    data class MyScheduleData(val scheduleItemId: Long) : KiboSerializable<MyScheduleData> {
        override fun getSerializer(): KSerializer<MyScheduleData> = serializer()
    }
}
