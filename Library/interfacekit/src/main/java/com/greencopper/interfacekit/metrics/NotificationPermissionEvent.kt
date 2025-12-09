package com.greencopper.interfacekit.metrics

import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.MappedMetrics
import com.greencopper.core.metrics.labels.itemName
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.permissions.AuthorizationStatus

public class NotificationPermissionEvent(
    private val authorizationStatus: AuthorizationStatus
) : MappedMetrics {

    private fun getAuthorizationStatusAnalytics() : String =
        when (authorizationStatus) {
            AuthorizationStatus.AuthorizedAlways -> "authorized"
            AuthorizationStatus.Denied -> "denied"
            else -> "Not determined"
        }

    public override fun track(provider: MappedProvider) {
        val eventName = EventName.notificationPermission()
        val parameters = mapOf(EventParameter.itemName to getAuthorizationStatusAnalytics())
        provider.track(eventName, parameters)
    }
}