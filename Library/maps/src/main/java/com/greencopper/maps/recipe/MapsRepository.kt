package com.greencopper.maps.recipe

import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.lists.ListRepository
import com.greencopper.maps.common.LocationData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.function.Predicate

public interface MapsRepository : ListRepository<LocationData> {
    public fun setConfiguration(configuration: MapsConfiguration)
    public fun getLocations(): List<LocationData>
    public fun getLocation(id: LocationDetailId): LocationData?
    public fun getLocationsRearranged(
        tagsFilter: Predicate<List<String>>? = null,
        sortedByName: Boolean = false
    ): List<LocationData>

    public fun clearConfig()

    override suspend fun getListData(predicate: FilteringPredicate?): Flow<List<LocationData>> =
        flowOf(getLocationsRearranged(predicate?.query()?.toPredicate(), true))
}
