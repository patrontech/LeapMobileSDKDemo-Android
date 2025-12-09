package com.greencopper.interfacekit.filtering

import com.greencopper.core.data.KiboSerializable
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
public data class FilteringInfo(
    val predicate: FilteringPredicate,
    val filters: Map<FilterId, FilterInfo> = emptyMap(),
) : KiboSerializable<FilteringInfo> {
    override fun getSerializer(): KSerializer<FilteringInfo> = serializer()

    internal constructor(state: FilteringState) : this(
        state.predicate,
        state.filters.mapValues { FilterInfo.build(it.value) }
    )
}

@Serializable(with = FilterSerializer::class)
public sealed class FilterInfo {
    @Required
    protected abstract val type: String

    internal companion object {
        fun build(filter: FilteringState.Filter): FilterInfo =
            when (filter) {
                is FilteringState.Filter.CheckBox -> CheckBox(
                    filter.label,
                    filter.operator,
                    filter.options.map { CheckBox.Option(it) },
                    filter.index
                )
            }
    }

    @Serializable
    internal data class CheckBox(
        val label: String,
        val operator: FilteringPredicate.Operator,
        val options: List<Option>,
        val index: Int,
    ) : FilterInfo() {
        @Required
        override val type: String = "checkbox"

        @Serializable
        internal data class Option(
            val label: String,
            val predicate: FilteringPredicate,
            val isActive: Boolean = false
        ) {
            internal constructor(option: FilteringState.Filter.CheckBox.Option) : this(
                option.label,
                option.predicate,
                option.isActive
            )
        }
    }
}

internal object FilterSerializer : KSerializer<FilterInfo> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FilterInfoSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FilterInfo) {
        when (value) {
            is FilterInfo.CheckBox -> encoder.encodeSerializableValue(
                FilterInfo.CheckBox.serializer(),
                value
            )
        }
    }

    override fun deserialize(decoder: Decoder): FilterInfo {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be loaded only by Json")
        val tree = input.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("Expected JsonObject")
        val serializer = when (tree["type"]?.jsonPrimitive?.content) {
            "checkbox" -> FilterInfo.CheckBox.serializer()
            else -> throw SerializationException("Couldn't decode correct sub-class of FilterInfo")
        }
        return input.json.decodeFromJsonElement(serializer, tree)
    }
}
