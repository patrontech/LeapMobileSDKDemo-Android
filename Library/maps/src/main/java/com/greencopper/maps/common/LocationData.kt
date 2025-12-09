package com.greencopper.maps.common

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.lists.ListRepository
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

public data class LocationData(
    override val itemId: String,
    val name: String,
    val subtitle: String? = null,
    val address: String? = null,
    val images: List<String> = emptyList(),
    val description: String? = null,
    val bottomWidgetCollection: WidgetCollectionConfiguration.Instance? = null,
    val tags: List<String> = emptyList(),
    val order: Int? = null,
) : ListRepository.Item<String>, KiboSerializable<LocationData> {
    override fun getSerializer(): KSerializer<LocationData> = serializer()
}
