package com.greencopper.interfacekit.empty

import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import kotlinx.serialization.Serializable

/**
 * Used in feature's data
 */
@Serializable
public data class EmptyPage(
    val image: String,
    val title: String,
    val subtitle: String,
    val topWidgetCollection: WidgetCollectionConfiguration.Instance? = null,
)
