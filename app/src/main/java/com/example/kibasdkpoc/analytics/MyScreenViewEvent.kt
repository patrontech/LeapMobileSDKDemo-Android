package com.example.kibasdkpoc.analytics

import com.greencopper.leapmobilesdk.core.metrics.events.screenName
import com.greencopper.leapmobilesdk.core.metrics.events.screenView
import com.greencopper.leapmobilesdk.core.metrics.labels.EventName
import com.greencopper.leapmobilesdk.core.metrics.labels.EventParameter
import com.greencopper.leapmobilesdk.core.metrics.labels.MappedMetrics
import com.greencopper.leapmobilesdk.core.metrics.labels.itemId
import com.greencopper.leapmobilesdk.core.metrics.labels.itemName
import com.greencopper.leapmobilesdk.core.metrics.provider.MappedProvider

public data class MyScreenViewEvent(
    val screenName: String
) : MappedMetrics {
    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.screenName to screenName
        )
        provider.track(EventName.screenView, parameters)
    }
}

public data class ProductPurchaseEvent(
    val productId: String,
    val productName: String,
    val price: Double,
    val screenName: String
) : MappedMetrics {
    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.itemId to productId,
            EventParameter.itemName to productName,
            EventParameter.screenName to screenName,
            EventParameter("price") to price.toString()
        )
        provider.track(EventName("purchase"), parameters)
    }
}

public data class ButtonClickEvent(
    val buttonName: String,
    val screenName: String
) : MappedMetrics {
    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.itemName to buttonName,
            EventParameter.screenName to screenName
        )
        provider.track(EventName("button_click"), parameters)
    }
}