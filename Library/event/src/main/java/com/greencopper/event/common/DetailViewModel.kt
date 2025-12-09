package com.greencopper.event.common

import androidx.lifecycle.ViewModel
import com.greencopper.event.activity.ui.viewdata.DetailViewData
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.widgets.resolver.WidgetCollectionResolver
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.toWidgetItems

internal abstract class DetailViewModel<T : Any>(
    private val widgetCollectionResolver: WidgetCollectionResolver,
    private val widgetResolver: WidgetResolver,
    private val mainFavoritesManager: FavoritesManager<T>,
) : ViewModel() {

    private var resolvedWidgetItems: List<WidgetCollectionView.WidgetItem>? = null

    fun getWidgetItems(widgetCollectionKey: String): List<WidgetCollectionView.WidgetItem> {
        return resolvedWidgetItems ?: run {
            val widgetInfos =
                widgetCollectionResolver.resolve(widgetCollectionKey)?.widgets ?: emptyList()
            widgetInfos.toWidgetItems(widgetResolver)
        }.also {
            resolvedWidgetItems = it
        }
    }

    fun addToFavorite(item: DetailViewData<T>) =
        mainFavoritesManager.addToFavorites(item)

    fun removeFromFavorite(item: DetailViewData<T>) =
        mainFavoritesManager.removeFromFavorites(item)
}
