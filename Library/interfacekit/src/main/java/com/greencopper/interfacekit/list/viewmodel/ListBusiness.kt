package com.greencopper.interfacekit.list.viewmodel

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.filtering.foldOr
import com.greencopper.interfacekit.interests.recipe.Interest
import com.greencopper.interfacekit.list.initializer.ListLayoutData
import com.greencopper.interfacekit.list.provider.ListProvider
import java.util.Locale
import kotlin.math.min

internal fun reloadItems(
    state: ListState,
    listData: ListLayoutData,
    unfilteredItems: List<ListProvider.Element>,
    favoriteIds: Set<Any>,
    configInterests: List<Interest>,
    myInterests: Set<String>,
    localizationService: LocalizationService,
    locale: Locale,
): ListState {

    val filteredItems: List<ListProvider.Element> = try {
        unfilteredItems
            .filterIsInFavorites(state.isInMyFavorites, favoriteIds)
            .filterByTags(state, configInterests, myInterests)
    } catch (e: LoadItemsException) {
        emptyList()
    }

    val contentState = if (filteredItems.isEmpty()) {
        getEmptyState(
            selectedList = state.selectedList,
            listData = listData,
            hasInterests = myInterests.isNotEmpty(),
        )
    } else {
        val columns = listData.mode.columns
        val resultItems: MutableList<ListViewData> = filteredItems
            .map { item ->
                item.toListViewData(
                    favoriteIds = favoriteIds,
                    localizationService = localizationService,
                    listData,
                )
            }.sortedWith(
                compareBy<ListViewData.ListItem, Int?>(nullsLast()) { it.order }
                    .thenBy { it.title.lowercase(locale) }
            ).toMutableList()
        var cptAddedWidgets = 0
        state.authorizedWidgets.forEach { indexedWidgets ->
            indexedWidgets.widgets.forEach {
                val insertIndex = indexedWidgets.index * columns + cptAddedWidgets++
                val finalIndex = min(insertIndex, resultItems.size)
                resultItems.add(
                    finalIndex,
                    ListViewData.WidgetItem(finalIndex, it)
                )
            }
        }

        ViewState.ContentState.Content(resultItems)
    }
    return state.copy(content = contentState)
}

internal fun getEmptyState(
    selectedList: Set<SelectedList>,
    listData: ListLayoutData,
    hasInterests: Boolean,
): ViewState.ContentState.Empty {
    val isInMyFavorites = selectedList.contains(SelectedList.MyFavorites)
    val isInMyInterests = selectedList.contains(SelectedList.MyInterests)

    val emptyPage = run {
        if (isInMyFavorites) {
            if (isInMyInterests && !hasInterests) {
                listData.myInterests?.emptyPage
            } else {
                listData.myFavorites?.emptyPage
            }
        } else if (isInMyInterests) {
            listData.myInterests?.emptyPage
        } else null
    } ?: listData.emptyPage

    return ViewState.ContentState.Empty(
        title = emptyPage.title,
        subtitle = emptyPage.subtitle,
        imageName = emptyPage.image,
        widgets = emptyPage.topWidgetCollection,
        screenName = listData.analytics.screenName,
    )
}

private fun List<ListProvider.Element>.filterByTags(
    state: ListState,
    configInterests: List<Interest>,
    myInterests: Set<String>,
): List<ListProvider.Element> {
    val myInterestsPredicate = if (state.isInMyInterests && configInterests.isNotEmpty()) {
        val tagsInterested = myInterests.mapNotNull { interestId ->
            configInterests.firstOrNull { it.id == interestId }?.tags
        }.flatten()

        if (tagsInterested.isEmpty()) throw LoadItemsException("No interests found")

        tagsInterested.foldOr()?.query()?.toPredicate()
    } else null

    val predicate = listOfNotNull(
        state.filteringPredicate?.toPredicate(),
        myInterestsPredicate,
    ).reduceOrNull { acc, predicate ->
        acc.and(predicate)
    }

    return predicate?.let { tagsPredicate ->
        filter { tagsPredicate.test(it.tags) }
    } ?: this
}

private fun List<ListProvider.Element>.filterIsInFavorites(
    isInMyFavorites: Boolean,
    favoriteIds: Set<Any>,
) =
    if (isInMyFavorites) {
        filter { listItem ->
            favoriteIds.contains(listItem.id)
        }
    } else this

private fun ListProvider.Element.toListViewData(
    favoriteIds: Set<Any>,
    localizationService: LocalizationService,
    listData: ListLayoutData,
): ListViewData.ListItem {
    val title = localizationService.getString(title)

    val isInMyFavorites = favoriteIds.contains(id)
    val favIcon =
        if (isInMyFavorites) listData.favoritesEditing?.remove
        else listData.favoritesEditing?.add
    val onTapAction =
        if (isInMyFavorites) ListAction.User.TappedRemoveFromMyFavorites(id, title)
        else ListAction.User.TappedAddToMyFavorites(id, title)

    return ListViewData.ListItem(
        id = id,
        title = title,
        subtitle = subtitle?.let { localizationService.getString(it) },
        image = image,
        order = order,
        favIcon = favIcon?.icon,
        favIconDescription = favIcon?.accessibilityLabel?.let { localizationService.getString(it) },
        onFavoriteTapAction = onTapAction
    )
}

private class LoadItemsException(msg: String) : Exception(msg)
