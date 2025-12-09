package com.greencopper.interfacekit.inbox

import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider

internal class InboxItemTap(private val itemName: String, private val itemId: String) :
    MappedMetrics {
    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.itemName to itemName,
            EventParameter.itemId to itemId
        )
        provider.track(EventName("inbox/item_tap"), parameters)
    }
}