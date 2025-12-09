package com.greencopper.interfacekit.metrics

import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.itemName
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.testmocks.core.MockingMappedProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LocationPermissionEventTest {

    private val mappingProvider = MockingMappedProvider()
    private val eventName = EventName.locationPermission()
    private val itemName = EventParameter.itemName

    init {
        mappingProvider.enable()
    }

    @Test
    fun whenTrackingLocationPermissionEvent_withStatusNotDetermined_itemNameShouldBeNotDetermined() {
        val event = LocationPermissionEvent(AuthorizationStatus.NotDetermined)
        val expectedItemName = "Not determined"
        event.track(mappingProvider)
        assertThat(
            mappingProvider.wasMetricTracked(
                eventName,
                mapOf(itemName to expectedItemName)
            )
        ).isTrue
    }

    @Test
    fun whenTrackingLocationPermissionEvent_withStatusDenied_itemNameShouldBeDenied() {
        val event = LocationPermissionEvent(AuthorizationStatus.Denied)
        val expectedItemName = "denied"
        event.track(mappingProvider)
        assertThat(
            mappingProvider.wasMetricTracked(
                eventName,
                mapOf(itemName to expectedItemName)
            )
        ).isTrue
    }

    @Test
    fun whenTrackingLocationPermissionEvent_withStatusAuthorizedUse_itemNameShouldBeAuthorizedUse() {
        val event = LocationPermissionEvent(AuthorizationStatus.AuthorizedWhenInUse)
        val expectedItemName = "authorizedWhenInUse"
        event.track(mappingProvider)
        assertThat(
            mappingProvider.wasMetricTracked(
                eventName,
                mapOf(itemName to expectedItemName)
            )
        ).isTrue
    }

    @Test
    fun whenTrackingLocationPermissionEvent_withStatusAuthorizedAlways_itemNameShouldBeAuthorizedAlways() {
        val event = LocationPermissionEvent(AuthorizationStatus.AuthorizedAlways)
        val expectedItemName = "authorizedAlways"
        event.track(mappingProvider)
        assertThat(
            mappingProvider.wasMetricTracked(
                eventName,
                mapOf(itemName to expectedItemName)
            )
        ).isTrue
    }
}