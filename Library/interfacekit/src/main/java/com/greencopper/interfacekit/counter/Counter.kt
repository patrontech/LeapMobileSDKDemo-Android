package com.greencopper.interfacekit.counter

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import com.greencopper.core.content.Key as CoreContentKey

public typealias CounterParameters = JsonElement

public interface Counter<T : KiboSerializable<T>> {
    public val key: Key
    public val params: CounterParameters
    public suspend fun count(dynamicPredicate: FilteringPredicate? = null): Int

    @Throws(SerializationException::class, ClassCastException::class)
    public fun deserialize(counterParams: CounterParameters): T

    @Serializable
    public data class Key(val name: String, val version: Int)
}

public fun <T : KiboSerializable<T>> Counter<T>.parseParams(logger: Logging): T? =
    try {
        deserialize(params)
    } catch (error: Throwable) {
        logger.e(message = "Couldn't decode counter params", throwable = error)
        null
    }
