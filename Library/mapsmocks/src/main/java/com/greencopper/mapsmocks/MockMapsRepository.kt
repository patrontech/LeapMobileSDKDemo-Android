package com.greencopper.mapsmocks

import com.greencopper.maps.common.LocationData
import com.greencopper.maps.recipe.*
import java.util.function.Predicate

public class MockMapsRepository(
    public var mockLocations: List<LocationData> = emptyList(),
    public var rearrangedLocations: List<LocationData> = emptyList(),
    public var rearrangedLocationsWithPredicate: List<LocationData> = emptyList(),
) : MapsRepository {

    public var config: MapsConfiguration? = null
    override fun setConfiguration(configuration: MapsConfiguration) {
        this.config = configuration
    }

    override fun getLocations(): List<LocationData> = mockLocations

    override fun getLocation(id: LocationDetailId): LocationData? = mockLocations.firstOrNull { it.itemId == id }

    override fun getLocationsRearranged(
        tagsFilter: Predicate<List<String>>?,
        sortedByName: Boolean
    ): List<LocationData> =
        tagsFilter?.let {
            rearrangedLocationsWithPredicate
        } ?: rearrangedLocations

    override fun clearConfig() {
        config = null
    }
}
