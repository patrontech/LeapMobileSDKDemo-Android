package com.greencopper.interfacekit.topbar

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ItemNameAnalytics
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
public data class TopBarData(
    public val title: String? = null,
    public val rightButtons: List<TopBarButton>? = null,
    public val leftButtons: List<TopBarButton>? = null,
) : KiboSerializable<TopBarData> {

    override fun getSerializer(): KSerializer<TopBarData> = serializer()
}

@Serializable(with = TopBarButtonSerializer::class)
public sealed class TopBarButton {

    @Required
    public abstract val type: String

    @Serializable
    public data class ImageButton(
        public val imageName: String,
        public val accessibilityLabel: String? = null,
        public val shouldColor: Boolean,
        public val onTap: OnTap? = null,
    ) : TopBarButton() {
        @Required
        override val type: String = "image"
    }

    @Serializable
    public data class TextButton(
        public val text: String,
        public val onTap: OnTap? = null,
    ): TopBarButton() {
        @Required
        override val type: String = "text"
    }

    @Serializable
    public data class OnTap(
        public val routeLink: String,
        public val analytics: ItemNameAnalytics,
    )
}

internal object TopBarButtonSerializer : KSerializer<TopBarButton> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TopBarButtonSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TopBarButton) {
        when (value) {
            is TopBarButton.ImageButton -> encoder.encodeSerializableValue(
                TopBarButton.ImageButton.serializer(),
                value
            )
            is TopBarButton.TextButton -> encoder.encodeSerializableValue(
                TopBarButton.TextButton.serializer(),
                value
            )
        }
    }

    override fun deserialize(decoder: Decoder): TopBarButton {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be loaded only by Json")
        val tree = input.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("Expected JsonObject")
        val serializer = when (tree["type"]?.jsonPrimitive?.content) {
            "image" -> TopBarButton.ImageButton.serializer()
            "text" -> TopBarButton.TextButton.serializer()
            else -> throw SerializationException("Couldn't decode correct sub-class of TopBarButton")
        }
        return input.json.decodeFromJsonElement(serializer, tree)
    }
}
