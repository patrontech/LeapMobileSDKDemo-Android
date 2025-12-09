package com.greencopper.maps.locationdetail

import androidx.lifecycle.ViewModel
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.tags.DisplayableTag
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.toWidgetItems
import com.greencopper.maps.recipe.MapsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class LocationDetailViewModel(
    private val localization: LocalizationService,
    private val mapsRepository: MapsRepository,
    private val widgetResolver: WidgetResolver,
    private val myLocationsManager: FavoritesManager<String>,
) : ViewModel() {

    fun getLocationDetails(locationId: String, displayableTags: List<DisplayableTag>): Flow<LocationDetailViewData?> =
        myLocationsManager.favoriteIdsFlow.map { favoriteLocations ->
            mapsRepository.getLocation(locationId)?.let { data ->
                LocationDetailViewData(
                    name = localization.getString(data.name),
                    subtitle = data.subtitle?.let { localization.getString(it) },
                    address = data.address?.let { localization.getString(it) },
                    descriptionTitle = localization.getString("maps.locationDetail.description_title"),
                    description = data.description?.let { localization.getString(it) },
                    images = data.images,
                    tags = displayableTags.filter { data.tags.contains(it.name) },
                    bottomWidgetCollection = data.bottomWidgetCollection,
                    isFavorite = favoriteLocations.contains(locationId)
                )
            }
        }

    private var resolvedWidgetItems: List<WidgetCollectionView.WidgetItem>? = null
    fun getWidgetItems(widgetInfos: List<WidgetCollectionConfiguration.Instance.WidgetInfo>): List<WidgetCollectionView.WidgetItem> {
        return resolvedWidgetItems ?: widgetInfos.toWidgetItems(widgetResolver).also {
            resolvedWidgetItems = it
        }
    }

    fun addToFavorite(item: Favoriteable<String>) =
        myLocationsManager.addToFavorites(item)

    fun removeFromFavorite(item: Favoriteable<String>) =
        myLocationsManager.removeFromFavorites(item)
}
