package com.greencopper.event.scheduleItem.viewmodel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Search(@SerialName("onTap") val onTapRouteLink: String)

@Serializable
internal data class TimelineData(
    val displayToggle: Boolean,
    val defaultDuration: Int = 45,
    val preferredTimeToWidthRatio: Int = 90,
    val buttonIcon: String,
    val emptyStateImage: String,
)




