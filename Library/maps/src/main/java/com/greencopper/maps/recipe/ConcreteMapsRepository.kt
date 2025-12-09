package com.greencopper.maps.recipe

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.maps.common.LocationData
import com.greencopper.toolkit.App
import java.util.function.Predicate

internal class ConcreteMapsRepository(private val localization: LocalizationService) :
    MapsRepository {
    private var currentConfiguration: MapsConfiguration? = null

    override fun setConfiguration(configuration: MapsConfiguration) {
        currentConfiguration = configuration
    }

    override fun getLocations(): List<LocationData> =
        currentConfiguration?.locations?.entries?.map { entry ->
            LocationData(
                itemId = entry.key,
                name = entry.value.name,
                subtitle = entry.value.subtitle,
                address = entry.value.address,
                images = entry.value.images ?: emptyList(),
                description = entry.value.description,
                bottomWidgetCollection = entry.value.bottomWidgetCollection,
                tags = entry.value.tags ?: emptyList(),
                order = entry.value.order,
            )
        } ?: listOf()

    override fun getLocation(id: LocationDetailId): LocationData? =
        getLocations().firstOrNull { it.itemId == id }

    override fun getLocationsRearranged(
        tagsFilter: Predicate<List<String>>?,
        sortedByName: Boolean
    ): List<LocationData> {
        val filteredLocations = getLocations()
            .filter {
                tagsFilter?.test(it.tags) ?: true
            }.toList()

        return if (sortedByName) {
            filteredLocations.sortedBy {
                localization.getString(it.name).lowercase(App.locale)
            }
        } else {
            filteredLocations
        }.sortedWith(compareBy(nullsLast()) { it.order })
    }

    override fun clearConfig() {
        currentConfiguration = null
    }
}
