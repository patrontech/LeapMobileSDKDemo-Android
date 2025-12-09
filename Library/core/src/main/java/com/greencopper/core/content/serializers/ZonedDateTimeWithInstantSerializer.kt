package com.greencopper.core.content.serializers

import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.ZonedDateTime

@Serializer(forClass = ZonedDateTime::class)
public object ZonedDateTimeWithInstantSerializer : KSerializer<ZonedDateTime> {

    private val timezoneProvider: TimezoneProvider = App.resolve()

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ZonedDateTimeWithInstantSerializer", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeLong(value.toInstant().toEpochMilli())
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(decoder.decodeLong()), timezoneProvider.zoneId)
    }
}
