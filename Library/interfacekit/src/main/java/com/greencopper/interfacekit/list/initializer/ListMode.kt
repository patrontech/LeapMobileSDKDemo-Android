package com.greencopper.interfacekit.list.initializer

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

@Serializable(with = ListModeSerializer::class)
public sealed class ListMode {
    @Required
    internal abstract val type: String

    internal open val columns: Int = 0

    @Serializable
    public data class Grid(
        override val columns: Int,
    ) : ListMode() {
        @Required
        override val type: String = Companion.type

        internal companion object {
            const val type: String = "grid"
        }
    }

    @Serializable
    public data class Table(
        val displayImages: Boolean,
    ) : ListMode() {
        @Required
        override val type: String = Companion.type

        override val columns: Int = 1

        internal companion object {
            const val type: String = "table"
        }
    }
}

internal object ListModeSerializer : KSerializer<ListMode> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ListModeSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ListMode) {
        when (value) {
            is ListMode.Grid -> encoder.encodeSerializableValue(
                ListMode.Grid.serializer(),
                value
            )

            is ListMode.Table -> encoder.encodeSerializableValue(
                ListMode.Table.serializer(),
                value
            )
        }
    }

    override fun deserialize(decoder: Decoder): ListMode {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be loaded only by Json")
        val tree = input.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("Expected JsonObject")

        val serializer = when (tree["type"]?.jsonPrimitive?.content) {
            ListMode.Grid.type -> ListMode.Grid.serializer()
            ListMode.Table.type -> ListMode.Table.serializer()
            else -> throw SerializationException("Couldn't decode correct sub-class of ListMode")
        }
        return input.json.decodeFromJsonElement(serializer, tree)
    }
}
