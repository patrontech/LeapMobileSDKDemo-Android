package com.greencopper.event.performers

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.event.metrics.addToMyPerformers_name
import com.greencopper.event.metrics.performersList_class
import com.greencopper.event.metrics.removeFromMyPerformers_name
import com.greencopper.event.performers.ui.performerslist.PerformersListFragment
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
import com.greencopper.interfacekit.topbar.TopBarData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.toLayoutData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class PerformersListInitializer : ParameterizedFeatureInitializer<PerformersListData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Event.PerformersList", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): PerformersListData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: PerformersListData): Layout = PerformersListFragment(
        PerformersListLayoutData(
            topBar = params.topBar,
            onPerformerTap = params.onPerformerTap,
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

    override fun redirectionHashForParams(params: PerformersListData): RedirectionHash =
        RedirectionHash(key, params.analytics.screenName)
}

@Serializable
internal data class PerformersListData(
    val topBar: TopBarData,
    val onPerformerTap: String,
    val filtering: FilteringInfo? = null,
    val search: Search? = null,
    val displayImages: Boolean = true,
    val collections: List<WidgetCollectionData>? = null,
    val favoritesEditing: FavoritesEditing? = null,
    val myFavorites: FavoriteConfig? = null,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<PerformersListData> {

    override fun getSerializer(): KSerializer<PerformersListData> = serializer()

    @Serializable
    data class Search(@SerialName("onTap") val onTapRouteLink: String)
}

@Serializable
internal data class PerformersListLayoutData(
    override val title: String? = null, //Deprecated, top bar title is in TopBarData
    val topBar: TopBarData,
    val onPerformerTap: String,
    override val filtering: FilteringInfo?,
    override val search: Search?,
    val displayImages: Boolean,
    override val widgetCollections: List<WidgetCollectionCellLayoutData>?,
    val favoritesEditing: FavoritesEditing?,
    override val myFavorites: FavoriteConfig?,
    override val analytics: ScreenNameAnalytics,
    override val redirectionHash: RedirectionHash,
) : ListData<PerformersListLayoutData> {
    override fun getSerializer(): KSerializer<PerformersListLayoutData> = serializer()
}

internal class PerformersListV2Initializer : ListInitializer() {
    override val providerKey: String = PerformersListProvider.key
    override val favoritesManagerKey: String = MyPerformersManager.diKey
    override val routeLinkKeyId: String = "performerId"

    override fun createAnalytics(listMode: ListMode, screenName: String): ListLayoutData.Analytics =
        ListLayoutData.Analytics(
            screenName = screenName,
            screenClass = performersList_class,
            addToMyFavoritesEventName = addToMyPerformers_name,
            removeFromMyFavoritesEventName = removeFromMyPerformers_name,
        )

    override val featureKey: FeatureKey = key

    companion object {
        val key: FeatureKey = FeatureKey("Event.PerformersList", 2)
    }

}
