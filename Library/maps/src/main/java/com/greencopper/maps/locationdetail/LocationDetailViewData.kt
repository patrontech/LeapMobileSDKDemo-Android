package com.greencopper.maps.locationdetail

import com.greencopper.interfacekit.tags.DisplayableTag
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration

internal class LocationDetailViewData(
    val name: String,
    val subtitle: String? = null,
    val address: String? = null,
    val images: List<String>? = null,
    val tags: List<DisplayableTag>,
    val descriptionTitle: String? = null,
    val description: String? = null,
    val bottomWidgetCollection: WidgetCollectionConfiguration.Instance? = null,
    val isFavorite: Boolean? = null,
)
