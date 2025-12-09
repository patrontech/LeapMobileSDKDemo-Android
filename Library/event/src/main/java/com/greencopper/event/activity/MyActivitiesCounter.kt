package com.greencopper.event.activity

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.counter.Counter
import com.greencopper.interfacekit.counter.CounterParameters
import com.greencopper.interfacekit.counter.parseParams
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.toolkit.logging.Logging
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class MyActivitiesCounter(
    override val params: CounterParameters,
    private val myActivitiesManager: FavoritesManager<Long>,
    private val logger: Logging,
) : Counter<MyActivitiesCounterParams> {
    override val key: Counter.Key = Companion.key

    override suspend fun count(dynamicPredicate: FilteringPredicate?): Int {
        val counterParams = parseParams(logger)
        val predicate = dynamicPredicate ?: counterParams?.predicate
        return myActivitiesManager.getFavoritesWithPredicate(predicate).size
    }

    override fun deserialize(counterParams: CounterParameters): MyActivitiesCounterParams =
        KiboSerializable.decodeFromJsonElement(counterParams)

    companion object {
        val key = Counter.Key("Event.Counter.MyActivities", 1)
    }
}

@Serializable
internal data class MyActivitiesCounterParams(val predicate: FilteringPredicate? = null) :
    KiboSerializable<MyActivitiesCounterParams> {
    override fun getSerializer(): KSerializer<MyActivitiesCounterParams> = serializer()
}
