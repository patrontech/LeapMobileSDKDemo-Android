package com.greencopper.interfacekit.list.initializer

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.color.DefaultColors
import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.interests.integration.IntegratedInterestsData
import com.greencopper.interfacekit.list.ui.ListFragment
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.topbar.TopBarData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.toLayoutData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public abstract class ListInitializer : ParameterizedFeatureInitializer<ListData>() {

    override fun decodeParams(params: FeatureParams): ListData =
        KiboSerializable.decodeFromJsonElement(params)

    protected abstract val providerKey: String
    protected abstract val favoritesManagerKey: String
    protected abstract val routeLinkKeyId: String
    protected abstract fun createAnalytics(listMode: ListMode, screenName: String): ListLayoutData.Analytics

    override fun layoutForParams(params: ListData): Layout = ListFragment(
        ListLayoutData(
            statusBarColor = params.statusBarColor?.let {
                DefaultColors.StatusBar(it, it)
            },
            topBar = params.topBar,
            mode = params.mode,
            onItemTapRouteLink = params.onItemTap,
            routeLinkKeyId = routeLinkKeyId,
            analytics = createAnalytics(params.mode, params.analytics.screenName),
            emptyPage = params.emptyPage,
            filtering = params.filtering,
            widgetCollections = params.collections?.map { it.toLayoutData() }?.sortedBy { it.index },
            favoritesEditing = params.favoritesEditing,
            myFavorites = params.myFavorites,
            myInterests = params.myInterests,
            providerKey = providerKey,
            favoritesManagerKey = favoritesManagerKey,
            redirectionHash = redirectionHashForParams(params)
        )
    )

    override fun redirectionHashForParams(params: ListData): RedirectionHash =
        RedirectionHash(featureKey, params.analytics.screenName)

}

@Serializable
public data class ListData(
    val statusBarColor: DefaultColors.StatusBar.Style? = null,
    val topBar: TopBarData,
    val mode: ListMode,
    val onItemTap: String,
    val analytics: ScreenNameAnalytics,
    val emptyPage: EmptyPage,
    val filtering: FilteringInfo? = null,
    val collections: List<WidgetCollectionData>? = null,
    val favoritesEditing: FavoritesEditing? = null,
    val myFavorites: FavoriteConfig? = null,
    val myInterests: IntegratedInterestsData? = null,
) : KiboSerializable<ListData> {

    override fun getSerializer(): KSerializer<ListData> = serializer()
}

@Serializable
public data class ListLayoutData(
    val statusBarColor: DefaultColors.StatusBar? = null,
    val topBar: TopBarData,
    val mode: ListMode,
    val onItemTapRouteLink: String,
    val routeLinkKeyId: String,
    val analytics: Analytics,
    val emptyPage: EmptyPage,
    val filtering: FilteringInfo? = null,
    val widgetCollections: List<WidgetCollectionCellLayoutData>? = null,
    val favoritesEditing: FavoritesEditing? = null,
    val myFavorites: FavoriteConfig? = null,
    val myInterests: IntegratedInterestsData? = null,
    val providerKey: String,
    val favoritesManagerKey: String,
    val redirectionHash: RedirectionHash,
) : KiboSerializable<ListLayoutData> {
    @Serializable
    public data class Analytics(
        val screenName: String,
        val screenClass: String,
        val addToMyFavoritesEventName: String,
        val removeFromMyFavoritesEventName: String,
    )

    override fun getSerializer(): KSerializer<ListLayoutData> = serializer()
}
