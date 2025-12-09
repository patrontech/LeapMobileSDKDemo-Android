package com.greencopper.interfacekit.metrics

import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.itemName
import com.greencopper.testmocks.core.MockingMappedProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TopBarTapEventTest {
    private val provider = MockingMappedProvider()

    init {
        provider.enable()
    }

    @Test
    fun whenItemNameIsNotNull_shouldTrack() {
        TopBarTapEvent("event")
            .track(provider)

        assertThat(
            provider.wasMetricTracked(
                EventName("top_bar/button_tap"),
                mapOf(EventParameter.itemName to "event")
            )
        ).isTrue
    }

    @Test
    fun whenItemNameIsNull_shouldTrack() {
        TopBarTapEvent(null)
            .track(provider)

        assertThat(
            provider.wasMetricTracked(
                EventName("top_bar/button_tap"),
                emptyMap()
            )
        ).isTrue
    }
}