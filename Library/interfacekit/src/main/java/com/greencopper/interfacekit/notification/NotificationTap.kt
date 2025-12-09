package com.greencopper.interfacekit.notification

import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider

internal class NotificationTap(private val itemId: String) :
    MappedMetrics {
    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.itemId to itemId
        )
        provider.track(EventName("notification/tap"), parameters)
    }
}