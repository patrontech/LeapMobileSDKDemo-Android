package com.greencopper.event.performers

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.counter.Counter
import com.greencopper.interfacekit.counter.CounterParameters
import com.greencopper.interfacekit.counter.parseParams
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.toolkit.logging.Logging
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class MyPerformersCounter(
    override val params: CounterParameters,
    private val myPerformersManager: FavoritesManager<String>,
    private val logger: Logging,
) : Counter<MyPerformersCounterParams> {

    override val key: Counter.Key = Companion.key

    override suspend fun count(dynamicPredicate: FilteringPredicate?): Int {
        val counterParams = parseParams(logger)
        val predicate = dynamicPredicate ?: counterParams?.predicate
        return myPerformersManager.getFavoritesWithPredicate(predicate).size
    }

    override fun deserialize(counterParams: CounterParameters): MyPerformersCounterParams =
        KiboSerializable.decodeFromJsonElement(counterParams)

    companion object {
        val key = Counter.Key("Event.Counter.MyPerformers", 1)
    }
}

@Serializable
internal data class MyPerformersCounterParams(val predicate: FilteringPredicate? = null) :
    KiboSerializable<MyPerformersCounterParams> {
    override fun getSerializer(): KSerializer<MyPerformersCounterParams> = serializer()
}
