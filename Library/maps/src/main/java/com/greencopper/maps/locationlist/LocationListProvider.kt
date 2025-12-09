package com.greencopper.maps.locationlist

import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.maps.recipe.MapsRepository

internal class LocationsListProvider(
    private val mapsRepository: MapsRepository,
) : ListProvider {

    companion object {
        const val key = "ListProvider.Locations"
    }

    override suspend fun getElements(): List<ListProvider.Element> {
        return mapsRepository.getLocations()
            .map {
                ListProvider.Element(
                    id = it.itemId,
                    order = it.order,
                    title = it.name,
                    subtitle = it.subtitle,
                    tags = it.tags,
                    image = it.images.firstOrNull()
                )
            }
    }
}
