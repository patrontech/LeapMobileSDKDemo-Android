package com.greencopper.interfacekit.interests.viewmodel

internal data class InterestsState(
    val title: String = "",
    val subtitle: String? = null,
    val buttonTitle: String = "",
    val interests: List<InterestState> = emptyList(),
)

internal data class InterestState(
    val id: String,
    val title: String,
    val selected: Boolean,
)
