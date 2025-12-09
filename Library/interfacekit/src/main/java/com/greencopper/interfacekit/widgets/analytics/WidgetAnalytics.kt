package com.greencopper.interfacekit.widgets.analytics

import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider

public fun buildWidgetAnalytics(
    widgetCategory: String,
    widgetName: String? = null,
    screenName: String,
    itemId: String? = null,
): MutableMap<EventParameter, String> {
    val analytics: MutableMap<EventParameter, String> = mutableMapOf()
    analytics[EventParameter.itemCategory] = widgetCategory
    analytics[EventParameter.screenName] = screenName
    widgetName?.let { analytics[EventParameter.itemName] = it }
    itemId?.let { analytics[EventParameter.itemId] = it }
    return analytics
}

public data class WidgetEventAnalytics(
    val eventName: EventName,
    val analytics: Map<EventParameter, String>,
) : MappedMetrics {
    override fun track(provider: MappedProvider) {
        provider.track(eventName, analytics)
    }
}
