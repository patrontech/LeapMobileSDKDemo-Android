package com.greencopper.interfacekit.filtering.filteringbar

import kotlinx.serialization.Serializable

@Serializable
public data class FilteringButton(val selected: Button, val unselected: Button) {
    @Serializable
    public data class Button(
        val icon: String? = null,
        val title: String? = null,
        val displayNumber: Boolean = false,
        val accessibilityLabel: String,
    )
}
