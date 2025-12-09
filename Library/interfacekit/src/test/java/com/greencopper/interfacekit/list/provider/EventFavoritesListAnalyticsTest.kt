package com.greencopper.interfacekit.list.provider

import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.testmocks.core.MockingMappedProvider
import com.greencopper.testmocks.shouldBe
import org.junit.jupiter.api.Test

internal class EventFavoritesListAnalyticsTest {

    @Test
    fun `track event should use provider properly`() {

        val mappingProvider = MockingMappedProvider().apply {
            enable()
        }

        val event = EventFavoritesListAnalytics(
            eventName = EventName(name = "eventName1"),
            screenName = "screenName1",
            itemId = "itemId1",
            itemName = "itemName1"
        )

        event.track(mappingProvider)

        mappingProvider.wasMetricTracked(
            event = EventName("eventName1"), parameters = mapOf(
                EventParameter.screenName to "screenName1",
                EventParameter.itemId to "itemId1",
                EventParameter.itemName to "itemName1"
            )
        ) shouldBe true
    }

}
