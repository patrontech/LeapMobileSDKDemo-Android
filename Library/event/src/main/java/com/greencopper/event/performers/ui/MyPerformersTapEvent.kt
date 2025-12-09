package com.greencopper.event.performers.ui

import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.event.metrics.addToMyPerformers
import com.greencopper.event.metrics.removeFromMyPerformers

public data class AddToMyPerformersAnalytics(
    val screenName: String,
    val itemId: String,
    val itemName: String
) : MappedMetrics {

    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.screenName to screenName,
            EventParameter.itemId to itemId,
            EventParameter.itemName to itemName
        )
        provider.track(
            EventName.addToMyPerformers(),
            parameters
        )
    }
}

public data class RemoveFromMyPerformersAnalytics(
    val screenName: String,
    val itemId: String,
    val itemName: String
) : MappedMetrics {

    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.screenName to screenName,
            EventParameter.itemId to itemId,
            EventParameter.itemName to itemName
        )
        provider.track(
            EventName.removeFromMyPerformers(),
            parameters
        )
    }
}
