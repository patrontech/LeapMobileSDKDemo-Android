package com.greencopper.maps.common.ui

import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.maps.metrics.addToMyLocations
import com.greencopper.maps.metrics.removeFromMyLocations

public data class AddToMyLocationsAnalytics(
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
            EventName.addToMyLocations(),
            parameters
        )
    }
}

public data class RemoveFromMyLocationsAnalytics(
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
            EventName.removeFromMyLocations(),
            parameters
        )
    }
}
