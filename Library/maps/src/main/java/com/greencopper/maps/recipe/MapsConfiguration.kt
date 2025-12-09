package com.greencopper.maps.recipe

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public typealias LocationDetailId = String

@Serializable
public data class MapsConfiguration(
    val locations: Map<LocationDetailId, LocationDetailConfigurationData>
): KiboSerializable<MapsConfiguration> {

    override fun getSerializer(): KSerializer<MapsConfiguration> = serializer()
}

@Serializable
public data class LocationDetailConfigurationData(
    val name: String,
    val subtitle: String? = null,
    val address: String? = null,
    val images: List<String>? = null,
    val description: String? = null,
    val bottomWidgetCollection: WidgetCollectionConfiguration.Instance? = null,
    val tags: List<String>? = null,
    val order: Int? = null,
)
