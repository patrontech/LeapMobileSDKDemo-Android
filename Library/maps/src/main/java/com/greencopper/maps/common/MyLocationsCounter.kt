package com.greencopper.maps.common

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.counter.Counter
import com.greencopper.interfacekit.counter.CounterParameters
import com.greencopper.interfacekit.counter.parseParams
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.toolkit.logging.Logging
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class MyLocationsCounter(
    override val params: CounterParameters,
    private val myLocationsManager: FavoritesManager<String>,
    private val logger: Logging,
) : Counter<MyLocationsCounterParams> {
    override val key: Counter.Key = Companion.key

    override suspend fun count(dynamicPredicate: FilteringPredicate?): Int {
        val counterParams = parseParams(logger)
        val predicate = dynamicPredicate ?: counterParams?.predicate
        return myLocationsManager.getFavoritesWithPredicate(predicate).size
    }

    override fun deserialize(counterParams: CounterParameters): MyLocationsCounterParams =
        KiboSerializable.decodeFromJsonElement(counterParams)

    companion object {
        val key = Counter.Key("Maps.Counter.MyLocations", 1)
    }
}

@Serializable
internal data class MyLocationsCounterParams(val predicate: FilteringPredicate? = null) :
    KiboSerializable<MyLocationsCounterParams> {
    override fun getSerializer(): KSerializer<MyLocationsCounterParams> = serializer()
}
