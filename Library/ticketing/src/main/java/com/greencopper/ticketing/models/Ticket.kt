package com.greencopper.ticketing.models

import com.greencopper.core.content.serializers.ZonedDateTimeWithInstantSerializer
import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
public data class Ticket(
    val primaryTitle: String,
    val primarySubtitle: String?,
    val qrCode: String,
    val secondaryTitle: String?,
    @Serializable(with = ZonedDateTimeWithInstantSerializer::class)
    val startDate: ZonedDateTime? = null,
) : KiboSerializable<Ticket> {
    override fun getSerializer(): KSerializer<Ticket> = serializer()
}
