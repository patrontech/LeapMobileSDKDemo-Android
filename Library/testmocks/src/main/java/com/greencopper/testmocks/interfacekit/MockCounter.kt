package com.greencopper.testmocks.interfacekit

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.counter.Counter
import com.greencopper.interfacekit.counter.CounterParameters
import com.greencopper.interfacekit.filtering.FilteringPredicate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public class MockCounter(
    override val key: Counter.Key,
    override val params: CounterParameters,
    public val items: Set<Item> = emptySet()
) : Counter<MockCounter.MockCounterParams> {

    override suspend fun count(dynamicPredicate: FilteringPredicate?): Int {
        val counterParams = try {
            deserialize(params)
        } catch (_: Throwable) {
            null
        }
        val finalPredicate = dynamicPredicate ?: counterParams?.predicate

        return finalPredicate?.query()?.toPredicate()?.let { predicate ->
            items.filter { predicate.test(it.tags) }.size
        } ?: items.size
    }

    override fun deserialize(counterParams: CounterParameters): MockCounterParams =
        KiboSerializable.decodeFromJsonElement(counterParams)

    @Serializable
    public data class MockCounterParams(val predicate: FilteringPredicate? = null) : KiboSerializable<MockCounterParams> {
        override fun getSerializer(): KSerializer<MockCounterParams> = serializer()
    }

    @Serializable
    public data class Item(val tags: List<String> = emptyList())
}
