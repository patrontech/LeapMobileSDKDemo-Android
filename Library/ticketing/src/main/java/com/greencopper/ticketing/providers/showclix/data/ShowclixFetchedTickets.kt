package com.greencopper.ticketing.providers.showclix.data

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ShowclixFetchedTickets(val tickets: List<Ticket>) :
    KiboSerializable<ShowclixFetchedTickets> {
    override fun getSerializer(): KSerializer<ShowclixFetchedTickets> = serializer()

    @Serializable
    data class Ticket(
        val name: String,
        val id: String,
        val void: Boolean,
        @SerialName("event_start_time_iso") val startDate: String? = null,
    )
}
