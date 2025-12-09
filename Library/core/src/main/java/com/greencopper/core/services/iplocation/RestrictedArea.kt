package com.greencopper.core.services.iplocation

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = RestrictedAreaSerializer::class)
public enum class RestrictedArea {
    IN_RESTRICTED_AREA,
    OUTSIDE_RESTRICTED_AREA
}

internal class RestrictedAreaSerializer: KSerializer<RestrictedArea> {
    private companion object {
        const val IN_RESTRICTED_AREA = "inRestrictedArea"
        const val OUTSIDE_RESTRICTED_AREA = "outsideRestrictedArea"
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "RestrictedArea",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: RestrictedArea) =
        encoder.encodeString(
            when (value) {
                RestrictedArea.IN_RESTRICTED_AREA -> IN_RESTRICTED_AREA
                RestrictedArea.OUTSIDE_RESTRICTED_AREA -> OUTSIDE_RESTRICTED_AREA
            }
        )

    override fun deserialize(decoder: Decoder): RestrictedArea {
        return when (val value = decoder.decodeString()) {
            IN_RESTRICTED_AREA -> RestrictedArea.IN_RESTRICTED_AREA
            OUTSIDE_RESTRICTED_AREA -> RestrictedArea.OUTSIDE_RESTRICTED_AREA
            else -> throw SerializationException("Unknown value '$value' for RestrictedArea.")
        }
    }
}
