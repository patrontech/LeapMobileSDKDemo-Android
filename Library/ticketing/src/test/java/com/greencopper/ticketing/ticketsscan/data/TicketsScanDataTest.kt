package com.greencopper.ticketing.ticketsscan.data

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.testmocks.setupTest
import com.greencopper.ticketing.providers.ProviderInfo
import com.greencopper.ticketing.ticketsscan.TicketsScanData
import com.greencopper.ticketingmocks.providers.MockProvider
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class TicketsScanDataTest {
    init {
        Toolkit.setupTest()
    }

    private val originalData = TicketsScanData(
        ProviderInfo(
            ProviderInfo.Key(
                "id123",
                1
            ),
            MockProvider.Data("apiUrl").encodeToJsonElement()
        ),
        "featureLink",
        ScreenNameAnalytics("screenName")
    )

    @Test
    fun serializeAndDeserialize() {
        assertDoesNotThrow {
            val data =
                KiboSerializable.decodeFromString<TicketsScanData>(originalData.encodeToString())
            Assertions.assertThat(data.provider).isEqualTo(originalData.provider)
            Assertions.assertThat(data.featureLink).isEqualTo(originalData.featureLink)
            Assertions.assertThat(data.analytics).isEqualTo(originalData.analytics)
        }
    }
}
