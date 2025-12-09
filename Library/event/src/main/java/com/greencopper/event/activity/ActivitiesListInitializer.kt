package com.greencopper.event.activity

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.event.activity.ui.activitylist.ActivitiesListFragment
import com.greencopper.event.metrics.activitiesList_class
import com.greencopper.event.metrics.addToMyActivities_name
import com.greencopper.event.metrics.removeFromMyActivities_name
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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class ActivitiesListInitializer : ParameterizedFeatureInitializer<ActivitiesListData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Event.ActivitiesList", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): ActivitiesListData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: ActivitiesListData): Layout = ActivitiesListFragment(
        ActivitiesListLayoutData(
            title = params.title,
            onActivityTap = params.onActivityTap,
            filtering = params.filtering,
            search = Search.build(params.search?.onTapRouteLink),
            displayImages = params.displayImages,
            widgetCollections = params.collections?.map { it.toLayoutData() },
            favoritesEditing = params.favoritesEditing,
            myFavorites = params.myFavorites,
            analytics = params.analytics,
            redirectionHash = redirectionHashForParams(params)
        )
    )

    override fun redirectionHashForParams(params: ActivitiesListData): RedirectionHash =
        RedirectionHash(key, params.analytics.screenName)
}

@Serializable
internal data class ActivitiesListData(
    val title: String? = null,
    val onActivityTap: String,
    val filtering: FilteringInfo? = null,
    val search: Search? = null,
    val displayImages: Boolean = true,
    val collections: List<WidgetCollectionData>? = null,
    val favoritesEditing: FavoritesEditing? = null,
    val myFavorites: FavoriteConfig? = null,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<ActivitiesListData> {

    override fun getSerializer(): KSerializer<ActivitiesListData> = serializer()

    @Serializable
    data class Search(@SerialName("onTap") val onTapRouteLink: String)
}

@Serializable
internal data class ActivitiesListLayoutData(
    override val title: String?,
    val onActivityTap: String,
    override val filtering: FilteringInfo?,
    override val search: Search?,
    val displayImages: Boolean,
    override val widgetCollections: List<WidgetCollectionCellLayoutData>? = null,
    val favoritesEditing: FavoritesEditing? = null,
    override val myFavorites: FavoriteConfig? = null,
    override val analytics: ScreenNameAnalytics,
    override val redirectionHash: RedirectionHash,
) : ListData<ActivitiesListLayoutData> {
    override fun getSerializer(): KSerializer<ActivitiesListLayoutData> = serializer()
}

internal class ActivitiesListV2Initializer : ListInitializer() {
    override val providerKey: String = ActivitiesListProvider.key
    override val favoritesManagerKey: String = MyActivitiesManager.diKey
    override val routeLinkKeyId: String = "activityId"

    override fun createAnalytics(listMode: ListMode, screenName: String): ListLayoutData.Analytics =
        ListLayoutData.Analytics(
            screenName = screenName,
            screenClass = activitiesList_class,
            addToMyFavoritesEventName = addToMyActivities_name,
            removeFromMyFavoritesEventName = removeFromMyActivities_name,
        )

    override val featureKey: FeatureKey = key

    companion object {
        val key: FeatureKey = FeatureKey("Event.ActivitiesList", 2)
    }
}
