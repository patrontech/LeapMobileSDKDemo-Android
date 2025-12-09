package com.greencopper.event.activity.ui

import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.event.metrics.addToMyActivities
import com.greencopper.event.metrics.removeFromMyActivities

public data class AddToMyActivitiesAnalytics(
    val screenName: String,
    val itemId: Long,
    val itemName: String
) : MappedMetrics {

    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.screenName to screenName,
            EventParameter.itemId to itemId.toString(),
            EventParameter.itemName to itemName
        )
        provider.track(
            EventName.addToMyActivities(),
            parameters
        )
    }
}

public data class RemoveFromMyActivitiesAnalytics(
    val screenName: String,
    val itemId: Long,
    val itemName: String
) : MappedMetrics {

    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.screenName to screenName,
            EventParameter.itemId to itemId.toString(),
            EventParameter.itemName to itemName
        )
        provider.track(
            EventName.removeFromMyActivities(),
            parameters
        )
    }
}
