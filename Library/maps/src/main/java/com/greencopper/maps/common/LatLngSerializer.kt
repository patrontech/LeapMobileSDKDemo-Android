package com.greencopper.maps.common

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public object LatLngSerializer : KSerializer<LatLng> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LatLngSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LatLng) {
        encoder.encodeString("${value.latitude},${value.longitude}")
    }

    override fun deserialize(decoder: Decoder): LatLng {
        val string = decoder.decodeString()
        val subs = string.split(",")
        return LatLng(subs[0].toDouble(), subs[1].toDouble())
    }
}

public object LatLngBoundsSerializer : KSerializer<LatLngBounds> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LatLngBoundsSerializer", PrimitiveKind.STRING)

    private val innerSerializer = ListSerializer(LatLngSerializer)

    override fun serialize(encoder: Encoder, value: LatLngBounds) {
        encoder.encodeSerializableValue(
            innerSerializer,
            listOf(
                value.northeast,
                value.southwest
            ),
        )
    }

    override fun deserialize(decoder: Decoder): LatLngBounds {
        return decoder.decodeSerializableValue(innerSerializer).toBounds()
    }
}
