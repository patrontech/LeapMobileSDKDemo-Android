package com.greencopper.maps.locationlist

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.list.initializer.ListInitializer
import com.greencopper.interfacekit.list.initializer.ListLayoutData
import com.greencopper.interfacekit.list.initializer.ListMode
import com.greencopper.interfacekit.lists.ListData
import com.greencopper.interfacekit.lists.Search
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.toLayoutData
import com.greencopper.maps.common.MyLocationsManager
import com.greencopper.maps.locationlist.ui.LocationListFragment
import com.greencopper.maps.metrics.addToMyLocations_name
import com.greencopper.maps.metrics.locationsList_class
import com.greencopper.maps.metrics.removeFromMyLocations_name
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class LocationListInitializer : ParameterizedFeatureInitializer<LocationListData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Maps.LocationsList", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): LocationListData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: LocationListData): Layout = LocationListFragment(
        LocationListLayoutData(
            analytics = params.analytics,
            filtering = params.filtering,
            title = params.title,
            displayImages = params.displayImages,
            search = Search.build(params.search?.onTapRouteLink),
            onLocationTap = params.onLocationTap,
            favoritesEditing = params.favoritesEditing,
            myFavorites = params.myFavorites,
            widgetCollections = params.collections?.map { it.toLayoutData() },
            redirectionHash = redirectionHashForParams(params),
        )
    )

    override fun redirectionHashForParams(params: LocationListData): RedirectionHash =
        RedirectionHash(key, params.analytics.screenName)
}

@Serializable
internal data class LocationListData(
    val analytics: ScreenNameAnalytics,
    val title: String? = null,
    val displayImages: Boolean = true,
    val filtering: FilteringInfo? = null,
    val search: Search? = null,
    val onLocationTap: String,
    val favoritesEditing: FavoritesEditing? = null,
    val myFavorites: FavoriteConfig? = null,
    val collections: List<WidgetCollectionData>? = null,
) : KiboSerializable<LocationListData> {

    override fun getSerializer(): KSerializer<LocationListData> = serializer()

    @Serializable
    data class Search(@SerialName("onTap") val onTapRouteLink: String)
}

@Serializable
internal data class LocationListLayoutData(
    override val analytics: ScreenNameAnalytics,
    override val filtering: FilteringInfo?,
    override val title: String?,
    val displayImages: Boolean,
    override val search: Search?,
    val onLocationTap: String,
    val favoritesEditing: FavoritesEditing? = null,
    override val myFavorites: FavoriteConfig? = null,
    override val widgetCollections: List<WidgetCollectionCellLayoutData>? = null,
    override val redirectionHash: RedirectionHash,
) : ListData<LocationListLayoutData> {
    override fun getSerializer(): KSerializer<LocationListLayoutData> = serializer()
}

internal class LocationsListV2Initializer : ListInitializer() {
    override val providerKey: String = LocationsListProvider.key
    override val favoritesManagerKey: String = MyLocationsManager.diKey
    override val routeLinkKeyId: String = "locationId"

    override fun createAnalytics(listMode: ListMode, screenName: String): ListLayoutData.Analytics =
        ListLayoutData.Analytics(
            screenName = screenName,
            screenClass = locationsList_class,
            addToMyFavoritesEventName = addToMyLocations_name,
            removeFromMyFavoritesEventName = removeFromMyLocations_name,
        )

    override val featureKey: FeatureKey = key

    companion object {
        val key: FeatureKey = FeatureKey("Maps.LocationsList", 2)
    }
}
