package com.greencopper.interfacekit.metrics

import com.greencopper.core.metrics.labels.*
import com.greencopper.interfacekit.notification.NotificationTap
import com.greencopper.testmocks.core.MockingMappedProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class NotificationTapEventTest {
    private val provider = MockingMappedProvider()

    init {
        provider.enable()
    }

    @Test
    fun track_shouldTrack() {
        NotificationTap("itemId")
            .track(provider)

        assertThat(
            provider.wasMetricTracked(
                EventName("notification/tap"),
                mapOf(EventParameter.itemId to "itemId")
            )
        ).isTrue
    }
}