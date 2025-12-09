package com.greencopper.interfacekit.tabBar

import com.greencopper.core.conditions.ConditionSet
import com.greencopper.core.conditions.Conditioned
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.route.Route
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class TabBarData(val selectedIndex: Int, val trackMetadata: Boolean? = true, val items: List<Item>): KiboSerializable<TabBarData> {

    @Serializable
    data class Item(
        val name: String,
        val iconName: String,
        val display: Display,
        override val conditionSet: ConditionSet? = null,
        val analytics: ItemNameAnalytics,
    ) : Conditioned

    @Serializable(with = DisplaySerializer::class)
    sealed class Display {
        abstract val mode: NavigationMode

        @Serializable
        data class Embedded(val feature: FeatureInfo) : Display() {
            override val mode: NavigationMode = NavigationMode.EMBEDDED
        }

        @Serializable
        data class Routing(val route: Route) : Display() {
            override val mode: NavigationMode = NavigationMode.ROUTING
        }
    }

    @Serializable
    enum class NavigationMode {
        @SerialName("embedded")
        EMBEDDED,

        @SerialName("routing")
        ROUTING
    }

    override fun getSerializer(): KSerializer<TabBarData> = serializer()
}

internal object DisplaySerializer : KSerializer<TabBarData.Display> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TabBarParameters.DisplaySerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TabBarData.Display) {
        when (value) {
            is TabBarData.Display.Embedded -> encoder.encodeSerializableValue(
                TabBarData.Display.Embedded.serializer(),
                value
            )
            is TabBarData.Display.Routing -> encoder.encodeSerializableValue(
                TabBarData.Display.Routing.serializer(),
                value
            )
        }
    }

    override fun deserialize(decoder: Decoder): TabBarData.Display {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This class can be loaded only by Json")
        val tree = input.decodeJsonElement() as? JsonObject ?: throw SerializationException("Expected JsonObject")
        if (tree["feature"] != null) {
            return input.json.decodeFromJsonElement(
                TabBarData.Display.Embedded.serializer(),
                tree
            )
        }
        if (tree["route"] != null) {
            return input.json.decodeFromJsonElement(
                TabBarData.Display.Routing.serializer(),
                tree
            )
        }
        throw SerializationException("Couldn't decode correct sub-class of Display")
    }
}
