package com.greencopper.event.scheduleItem

import com.greencopper.core.data.KiboSerializable
import com.greencopper.event.scheduleItem.data.repository.ScheduleItemRepository
import com.greencopper.interfacekit.counter.Counter
import com.greencopper.interfacekit.counter.CounterParameters
import com.greencopper.interfacekit.counter.parseParams
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.toolkit.logging.Logging
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class MyScheduleCounter(
    override val params: CounterParameters,
    private val scheduleItemRepository: ScheduleItemRepository,
    private val myScheduleManager: FavoritesManager<Long>,
    private val logger: Logging,
) : Counter<MyScheduleCounterParams> {

    override val key: Counter.Key = Companion.key

    override suspend fun count(dynamicPredicate: FilteringPredicate?): Int {
        val counterParams = parseParams(logger)
        val predicate = dynamicPredicate ?: counterParams?.predicate
        val scheduleItems = scheduleItemRepository.getScheduleItemsForTags(predicate?.query()?.toSQL())
            .first()
            .map { it.itemId }
        return myScheduleManager.favoriteIds.intersect(scheduleItems.toSet()).size
    }

    override fun deserialize(counterParams: CounterParameters): MyScheduleCounterParams =
        KiboSerializable.decodeFromJsonElement(counterParams)

    companion object {
        val key = Counter.Key("Event.Counter.MySchedule", 1)
    }
}

@Serializable
internal data class MyScheduleCounterParams(val predicate: FilteringPredicate? = null) :
    KiboSerializable<MyScheduleCounterParams> {
    override fun getSerializer(): KSerializer<MyScheduleCounterParams> = serializer()
}
