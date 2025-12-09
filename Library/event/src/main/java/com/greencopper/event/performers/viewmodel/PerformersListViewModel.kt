package com.greencopper.event.performers.viewmodel

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.event.performers.data.repository.PerformerRepository
import com.greencopper.event.performers.ui.performerslist.PerformersListItem
import com.greencopper.event.performers.ui.performerslist.toListItem
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.lists.ListViewModel
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import kotlinx.coroutines.flow.*
import java.util.Locale

internal class PerformersListViewModel(
    private val performersRepository: PerformerRepository,
    filteringHandler: FilteringHandler,
    widgetResolver: WidgetResolver,
    localizationService: LocalizationService,
    val myPerformersManager: FavoritesManager<String>,
    locale: Locale,
) : ListViewModel<PerformersListItem>(
    filteringHandler,
    widgetResolver,
    localizationService,
    myPerformersManager,
    locale
) {
    override fun getItems(widgetCollections: List<WidgetCollectionCellLayoutData>?): Flow<List<PerformersListItem>> =
        combine(
            myPerformersManager.favoriteIdsFlow,
            getPerformersForTags()
        ) { favoriteIds, performers ->
            val filteredPerformers: MutableList<PerformersListItem> = performers
                .map {
                    it.toListItem(localizationService, favoriteIds)
                }.sortedWith(
                    compareBy<PerformersListItem.Card, Int?>(nullsLast()) { it.order }
                        .thenBy { it.name.lowercase(locale) }
                ).toMutableList()

            if (filteringHandler.currentMode == FilteringHandler.Mode.MY_FAVORITES) {
                filteredPerformers.removeAll {
                    (it as? PerformersListItem.Card)?.let { item ->
                        !favoriteIds.contains(item.itemId)
                    } ?: false
                }
            }

            if (filteredPerformers.size > 0 && widgetCollections != null) {
                var addWidgetCollections = 0
                getSortedWidgetItems(widgetCollections).forEach {
                    val indexToInsert = it.key + addWidgetCollections++
                    val widgetCollection =
                        PerformersListItem.WidgetCollectionHolder(it.key, it.value)
                    if (filteredPerformers.size > indexToInsert) {
                        filteredPerformers.add(
                            indexToInsert,
                            widgetCollection
                        )
                    } else {
                        filteredPerformers.add(widgetCollection)
                    }
                }
            }

            filteredPerformers
        }

    private fun getPerformersForTags() = filteringHandler.predicate.flatMapLatest { query ->
        performersRepository.getPerformersForTags(query?.toSQL())
    }
}
