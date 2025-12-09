package com.greencopper.maps.locationlist

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.lists.ListViewModel
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.maps.locationlist.ui.LocationListItem
import com.greencopper.maps.recipe.MapsRepository
import kotlinx.coroutines.flow.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.util.Locale

internal class LocationListViewModel(
    private val mapsRepository: MapsRepository,
    private val localization: LocalizationService,
    filteringHandler: FilteringHandler,
    widgetResolver: WidgetResolver,
    val myLocationsManager: FavoritesManager<String>,
    locale: Locale,
) : ListViewModel<LocationListItem>(filteringHandler, widgetResolver, localization, myLocationsManager, locale) {

    override fun getItems(widgetCollections: List<WidgetCollectionCellLayoutData>?): Flow<List<LocationListItem>> =
        combine(
            myLocationsManager.favoriteIdsFlow,
            getLocationsForTags()
        ) { favoriteIds, locations ->
            val filteredLocations: MutableList<LocationListItem> = locations
                .map { item ->
                    LocationListItem.LocationItem(
                        itemId = item.itemId,
                        name = localization.getString(item.name),
                        subtitle = item.subtitle?.let { localization.getString(it) },
                        photo = item.images.firstOrNull(),
                        isFavorite = favoriteIds.contains(item.itemId),
                        order = item.order
                    )
                }.sortedWith(
                    compareBy<LocationListItem.LocationItem, Int?>(nullsLast()) { it.order }
                        .thenBy { it.name.lowercase(locale) }
                )
                .toMutableList()

            if (filteringHandler.currentMode == FilteringHandler.Mode.MY_FAVORITES) {
                filteredLocations.removeAll {
                    (it as? LocationListItem.LocationItem)?.let { item ->
                        !favoriteIds.contains(item.itemId)
                    } ?: false
                }
            }

            if (filteredLocations.size > 0 && widgetCollections != null) {
                var addWidgetCollections = 0
                getSortedWidgetItems(widgetCollections).forEach {
                    val indexToInsert = it.key + addWidgetCollections++
                    val widgetCollection =
                        LocationListItem.WidgetCollectionHolder(it.key, it.value)
                    if (filteredLocations.size > indexToInsert) {
                        filteredLocations.add(
                            indexToInsert,
                            widgetCollection
                        )
                    } else {
                        filteredLocations.add(widgetCollection)
                    }
                }
            }

            filteredLocations
        }

    private fun getLocationsForTags() =
        filteringHandler.predicate.mapLatest { query ->
            mapsRepository.getLocationsRearranged(query?.toPredicate(), true)
        }

    @Serializable
    data class SavedFiltering(val mode: FilteringHandler.Mode, val filteringInfo: FilteringInfo? = null) :
        KiboSerializable<SavedFiltering> {
        override fun getSerializer(): KSerializer<SavedFiltering> = serializer()
    }
}
