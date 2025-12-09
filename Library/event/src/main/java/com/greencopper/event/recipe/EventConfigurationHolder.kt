package com.greencopper.event.recipe

import kotlinx.coroutines.flow.MutableStateFlow

internal class EventConfigurationHolder {
    val currentConfiguration: MutableStateFlow<EventConfiguration?> = MutableStateFlow(null)
}