package com.greencopper.interfacekit.favorites

import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.filtering.filteringbar.FilteringButton
import kotlinx.serialization.Serializable

@Serializable
public data class FavoriteConfig(
    val activeOnLoad: Boolean = false,
    val filteringButton: FilteringButton? = null,
    val filtering: FilteringInfo? = null,
    val emptyPage: EmptyPage,
    val showPicker: Boolean = true,
)
