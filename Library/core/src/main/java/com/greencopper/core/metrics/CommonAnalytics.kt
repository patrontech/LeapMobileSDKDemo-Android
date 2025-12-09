package com.greencopper.core.metrics

import kotlinx.serialization.Serializable

@Serializable
public data class ScreenNameAnalytics(val screenName: String)

@Serializable
public data class ItemNameAnalytics(val itemName: String)

@Serializable
public data class ItemNameIdAnalytics(
    val itemName: String,
    val itemId: String,
)
