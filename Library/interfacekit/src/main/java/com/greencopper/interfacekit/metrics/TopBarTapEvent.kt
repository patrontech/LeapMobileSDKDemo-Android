package com.greencopper.interfacekit.metrics

import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.MappedMetrics
import com.greencopper.core.metrics.labels.itemName
import com.greencopper.core.metrics.provider.MappedProvider


internal data class TopBarTapEvent(
    private val itemName: String?
) : MappedMetrics {
    override fun track(provider: MappedProvider) {
        val eventName = EventName("top_bar/button_tap")
        val parameters = mutableMapOf<EventParameter, String>()
        itemName?.let {
            parameters.put(EventParameter.itemName, itemName)
        }
        provider.track(eventName, parameters)
    }
}