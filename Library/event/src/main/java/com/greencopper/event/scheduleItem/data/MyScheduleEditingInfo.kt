package com.greencopper.event.scheduleItem.data

import kotlinx.serialization.Serializable

@Serializable
internal data class MyScheduleEditingInfo(
    val add: ButtonDetail,
    val remove: ButtonDetail,
    val onMainScheduleItem: Boolean? = true,
) {

    @Serializable
    data class ButtonDetail(
        val icon: String,
        val accessibilityLabel: String,
    )
}
