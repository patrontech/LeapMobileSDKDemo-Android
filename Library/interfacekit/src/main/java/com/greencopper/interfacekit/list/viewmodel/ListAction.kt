package com.greencopper.interfacekit.list.viewmodel

import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.viewmodel.IndexedWidgets

internal sealed class ListAction {
    object User {
        data class TappedListItem(val listItemId: Any) : ListAction()

        data class TappedMyFavorites(val selected: Boolean) : ListAction()

        data class TappedMyInterests(val selected: Boolean) : ListAction()

        data class TappedAddToMyFavorites(
            val listItemId: Any,
            val itemName: String,
        ) : ListAction()

        data class TappedRemoveFromMyFavorites(
            val listItemId: Any,
            val itemName: String,
        ) : ListAction()

        data class FilteringUpdated(val filteringPredicate: FilteringPredicate.FilteringPredicateComputed?) :
            ListAction()

        data class FavoritesIdsUpdated(val ids: Set<Any>) : ListAction()
        data class InterestsUpdated(val interests: Set<String>) : ListAction()
    }

    data class ScreenLoaded(val layout: Layout, val uiClient: ListReducer.UiClient) : ListAction()

    data class WidgetsChanged(val widgets: List<IndexedWidgets>) : ListAction()
    data object ItemsReloaded : ListAction()
}
