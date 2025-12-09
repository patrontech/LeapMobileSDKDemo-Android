package com.greencopper.ticketing.providers.showclix.data

import com.greencopper.core.data.KiboSerializable
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class ShowclixFetchedTicketsTest {

    init {
        Toolkit.setupTest()
    }

    private val originalData = ShowclixFetchedTickets(
        listOf(
            ShowclixFetchedTickets.Ticket(
                "name",
                "id123",
                true
            )
        )
    )

    @Test
    fun serializeAndDeserialize() {
        assertDoesNotThrow {
            val data =
                KiboSerializable.decodeFromString<ShowclixFetchedTickets>(originalData.encodeToString())
            assertThat(data.tickets[0].name).isEqualTo(originalData.tickets[0].name)
            assertThat(data.tickets[0].id).isEqualTo(originalData.tickets[0].id)
            assertThat(data.tickets[0].void).isEqualTo(originalData.tickets[0].void)
        }
    }
}