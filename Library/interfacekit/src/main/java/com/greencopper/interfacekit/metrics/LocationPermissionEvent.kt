package com.greencopper.interfacekit.metrics

import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.MappedMetrics
import com.greencopper.core.metrics.labels.itemName
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.permissions.AuthorizationStatus

public class LocationPermissionEvent(
    private val authorizationStatus: AuthorizationStatus
) : MappedMetrics {

    private fun getAuthorizationStatusAnalytics() : String {
        return when (authorizationStatus) {
            AuthorizationStatus.AuthorizedAlways -> "authorizedAlways"
            AuthorizationStatus.AuthorizedWhenInUse -> "authorizedWhenInUse"
            AuthorizationStatus.Denied -> "denied"
            else -> "Not determined"
        }
    }

    public override fun track(provider: MappedProvider) {
        val eventName = EventName.locationPermission()
        val parameters = mapOf(EventParameter.itemName to getAuthorizationStatusAnalytics())
        provider.track(eventName, parameters)
    }
}