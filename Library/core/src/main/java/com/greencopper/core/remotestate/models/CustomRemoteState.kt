package com.greencopper.core.remotestate.models

import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable(with = CustomRemoteStateSerializer::class)
internal class CustomRemoteState private constructor(
    private val _containers: MutableMap<String, MutableMap<String, JsonElement>>
) {
    internal constructor(): this(mutableMapOf())

    internal constructor(jsonObject: JsonObject): this(
        jsonObject.mapValues { it.value.jsonObject.toMutableMap() }.toMutableMap()
    )

    internal inline fun <reified T> setCustom(key: String, container: String, custom: T) {
        this[key, container] = App.resolve<Json>().encodeToJsonElement(custom)
    }

    internal operator fun set(key: String, container: String, value: JsonElement) {
        _containers.getOrPut(container) { mutableMapOf() }[key] = value
    }

    internal operator fun get(key: String, container: String): JsonElement =
        _containers[container]?.get(key) ?: JsonNull

    internal fun toJson(): JsonObject =
        JsonObject(_containers.mapValues { JsonObject(it.value) })
}

/**
 * Why custom serialization? Because we will be querying this with the
 * JSON Query DSL, its serialized JSON format must match that used on
 * iOS **exactly**.
 */
private class CustomRemoteStateSerializer: KSerializer<CustomRemoteState> {
    override val descriptor: SerialDescriptor = JsonObject.serializer().descriptor

    override fun serialize(encoder: Encoder, value: CustomRemoteState) {
        encoder.encodeSerializableValue(JsonObject.serializer(), value.toJson())
    }

    override fun deserialize(decoder: Decoder): CustomRemoteState {
        val surrogate = decoder.decodeSerializableValue(JsonObject.serializer())
        return CustomRemoteState(surrogate)
    }
}