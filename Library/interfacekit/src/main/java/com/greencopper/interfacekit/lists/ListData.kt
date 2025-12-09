package com.greencopper.interfacekit.lists

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import kotlinx.serialization.Serializable

public interface ListData<T : KiboSerializable<T>> : KiboSerializable<T> {
    public val title: String?
    public val filtering: FilteringInfo?
    public val myFavorites: FavoriteConfig?
    public val redirectionHash: RedirectionHash
    public val widgetCollections: List<WidgetCollectionCellLayoutData>?
    public val search: Search?
    public val analytics: ScreenNameAnalytics
}

@Serializable
public data class Search(val onTapRouteLink: String) {
    public companion object {
        public fun build(routeLink: String?): Search? =
            routeLink?.let {
                Search(routeLink)
            }
    }
}
