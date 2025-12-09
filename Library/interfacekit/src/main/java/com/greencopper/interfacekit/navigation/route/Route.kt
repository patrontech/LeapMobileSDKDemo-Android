package com.greencopper.interfacekit.navigation.route

import android.os.Bundle
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.w
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable(with = RouteSerializer::class)
public sealed class Route {
    @Required
    internal abstract val mode: String

    @Serializable
    public data class Push(
        public val feature: FeatureInfo,
        public val addToBackStack: Boolean = true
    ) :
        Route() {
        @Required
        override val mode: String = "push"
    }

    @Serializable
    public data class Present(public val feature: FeatureInfo) : Route() {
        @Required
        override val mode: String = "present"
    }

    @Serializable
    public data class External(public val url: String, public val analytics: ScreenNameAnalytics? = null) : Route() {

        @Required
        override val mode: String = "external"
    }

    @Serializable
    public data class Execute(public val command: CommandInfo) : Route() {
        @Required
        override val mode: String = "execute"
    }

    public fun getFeatureInfo(): FeatureInfo? = when (this) {
        is Push -> feature
        is Present -> feature
        else -> null
    }

    internal fun getParams(): JsonElement? = when (this) {
        is Push -> feature.params
        is Present -> feature.params
        is Execute -> command.params
        else -> null
    }
}

internal object RouteSerializer : KSerializer<Route> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("RouteSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Route) {
        when (value) {
            is Route.Push -> encoder.encodeSerializableValue(
                Route.Push.serializer(),
                value
            )
            is Route.Present -> encoder.encodeSerializableValue(
                Route.Present.serializer(),
                value
            )
            is Route.External -> encoder.encodeSerializableValue(
                Route.External.serializer(),
                value
            )
            is Route.Execute -> encoder.encodeSerializableValue(
                Route.Execute.serializer(),
                value
            )
        }
    }

    override fun deserialize(decoder: Decoder): Route {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be loaded only by Json")
        val tree = input.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("Expected JsonObject")
        val serializer = when (tree["mode"]?.jsonPrimitive?.content) {
            "push" -> Route.Push.serializer()
            "present" -> Route.Present.serializer()
            "external" -> Route.External.serializer()
            "execute" -> Route.Execute.serializer()
            else -> throw SerializationException("Couldn't decode correct sub-class of Route")
        }
        return input.json.decodeFromJsonElement(serializer, tree)
    }
}

public fun Bundle.putRoute(key: String, route: Route) {
    putString(key, App.resolve<Json>().encodeToString(route))
}

public fun Bundle.getRoute(key: String): Route? {
    return try {
        App.resolve<Json>().decodeFromString<Route>(getString(key).orEmpty())
    } catch (t: Throwable) {
        App.log.w("Couldn't resolve route", throwable = t)
        null
    }
}
