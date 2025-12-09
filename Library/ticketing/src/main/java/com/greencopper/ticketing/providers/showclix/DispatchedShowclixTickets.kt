package com.greencopper.ticketing.providers.showclix

import com.greencopper.core.data.KiboSerializable
import com.greencopper.ticketing.providers.showclix.data.ShowclixFetchedTickets
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DispatchedShowclixTickets(
    val tickets: List<Ticket>,
) : KiboSerializable<DispatchedShowclixTickets> {
    override fun getSerializer(): KSerializer<DispatchedShowclixTickets> = serializer()

    companion object {
        const val dispatcherKey: String = "showclix"
    }
}

@Serializable
internal data class Ticket(
    val name: String,
    @SerialName("start_time") val startTime: String,
)

internal fun List<ShowclixFetchedTickets.Ticket>.toDispatchEntry(): DispatchedShowclixTickets {
    return map {
        Ticket(it.name, it.startDate ?: "")
    }.let {
        DispatchedShowclixTickets(it)
    }
}
