package com.greencopper.interfacekit.metrics

import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.itemName
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.testmocks.core.MockingMappedProvider
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class NotificationPermissionEventTest {
    private val mappingProvider = MockingMappedProvider()
    private val eventName = EventName.notificationPermission()
    private val itemName = EventParameter.itemName

    init {
        mappingProvider.enable()
    }

    @Test
    fun whenTrackingNotificationPermissionEvent_withStatusNotDetermined_itemNameShouldBeNotDetermined() {
        val event = NotificationPermissionEvent(AuthorizationStatus.NotDetermined)
        val expectedItemName = "Not determined"
        event.track(mappingProvider)
        Assertions.assertThat(
            mappingProvider.wasMetricTracked(
                eventName,
                mapOf(itemName to expectedItemName)
            )
        ).isTrue
    }

    @Test
    fun whenTrackingNotificationPermissionEvent_withStatusDenied_itemNameShouldBeDenied() {
        val event = NotificationPermissionEvent(AuthorizationStatus.Denied)
        val expectedItemName = "denied"
        event.track(mappingProvider)
        Assertions.assertThat(
            mappingProvider.wasMetricTracked(
                eventName,
                mapOf(itemName to expectedItemName)
            )
        ).isTrue
    }

    @Test
    fun whenTrackingNotificationPermissionEvent_withStatusAuthorizedAlways_itemNameShouldBeAuthorized() {
        val event = NotificationPermissionEvent(AuthorizationStatus.AuthorizedAlways)
        val expectedItemName = "authorized"
        event.track(mappingProvider)
        Assertions.assertThat(
            mappingProvider.wasMetricTracked(
                eventName,
                mapOf(itemName to expectedItemName)
            )
        ).isTrue
    }
}