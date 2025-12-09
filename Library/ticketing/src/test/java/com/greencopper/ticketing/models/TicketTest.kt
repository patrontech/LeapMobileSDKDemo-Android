package com.greencopper.ticketing.models

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.core.MockTimezoneProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class TicketTest {

    init {
        Toolkit.setupTest()
        bindProvider<TimezoneProvider>(MockTimezoneProvider())
    }

    private val originalTicket = Ticket(
        "primaryTitle",
        "primarySubtitle",
        "0123456789",
        "secondaryTitle",
        ZonedDateTime.now(),
    )

    @Test
    fun serializeAndDeserialize() {
        assertDoesNotThrow {
            val ticket = KiboSerializable.decodeFromString<Ticket>(originalTicket.encodeToString())
            assertThat(ticket.primaryTitle).isEqualTo(originalTicket.primaryTitle)
            assertThat(ticket.primarySubtitle).isEqualTo(originalTicket.primarySubtitle)
            assertThat(ticket.qrCode).isEqualTo(originalTicket.qrCode)
            assertThat(ticket.secondaryTitle).isEqualTo(originalTicket.secondaryTitle)
            assertThat(ticket.startDate?.toInstant()
                ?.toEpochMilli()).isEqualTo(originalTicket.startDate?.toInstant()?.toEpochMilli())
        }
    }
}
