package com.greencopper.interfacekit.list.viewmodel

import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.interests.integration.IntegratedInterestsData
import com.greencopper.interfacekit.list.initializer.ListLayoutData
import com.greencopper.interfacekit.list.initializer.ListMode
import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.topbar.TopBarData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData

internal fun createListData(
    listMode: ListMode = ListMode.Grid(1),
    widgetCollections: List<WidgetCollectionCellLayoutData> = emptyList(),
    myFavorites: FavoriteConfig? = null,
    favoritesEditing: FavoritesEditing? = null,
    myInterests: IntegratedInterestsData? = null,
): ListLayoutData = ListLayoutData(
    statusBarColor = null,
    topBar = TopBarData(
        title = null,
        rightButtons = listOf(),
        leftButtons = listOf()
    ),
    mode = listMode,
    onItemTapRouteLink = "onItemTapRouteLink",
    routeLinkKeyId = "routeLinkKeyId",
    analytics = ListLayoutData.Analytics(
        screenName = "screenName",
        screenClass = "screenClass",
        addToMyFavoritesEventName = "addToMyFavoritesEventName",
        removeFromMyFavoritesEventName = "removeFromMyFavoritesEventName"
    ),
    emptyPage = EmptyPage(image = "emptyImage", title = "emptyTitle", subtitle = "emptySubtitle"),
    filtering = null,
    widgetCollections = widgetCollections,
    favoritesEditing = favoritesEditing,
    myFavorites = myFavorites,
    myInterests = myInterests,
    providerKey = "providerKey",
    favoritesManagerKey = "favoritesManagerKey",
    redirectionHash = RedirectionHash(
        featureKey = FeatureKey(
            name = "featureKey",
            version = 99
        ), identifier = null
    )

)

internal fun generateElements(
    numberOfItems: Int,
): MutableList<ListProvider.Element> =
    generateElements(items = (List(numberOfItems) { ElementGeneratorItem() }).toTypedArray())


internal fun generateElements(
    vararg items: ElementGeneratorItem,
): MutableList<ListProvider.Element> {
    return items.mapIndexed { index, item ->
        ListProvider.Element(
            id = index.toLong(),
            order = item.order,
            title = item.name ?: "title$index",
            subtitle = "subtitle$index",
            tags = listOf("tag$index"),
            image = "image$index",
        )
    }.toMutableList()
}

internal data class ElementGeneratorItem(
    val name: String? = null,
    val order: Int? = null,
)
