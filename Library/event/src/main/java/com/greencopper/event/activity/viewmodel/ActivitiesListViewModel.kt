package com.greencopper.event.activity.viewmodel

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.event.activity.data.repository.ActivityRepository
import com.greencopper.event.activity.ui.activitylist.ActivitiesListItem
import com.greencopper.event.activity.ui.activitylist.toListItem
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringHandler.Mode
import com.greencopper.interfacekit.lists.ListViewModel
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.util.Locale

internal class ActivitiesListViewModel(
    private val activityRepository: ActivityRepository,
    filteringHandler: FilteringHandler,
    widgetResolver: WidgetResolver,
    localizationService: LocalizationService,
    val myActivitiesManager: FavoritesManager<Long>,
    locale: Locale,
) : ListViewModel<ActivitiesListItem>(
    filteringHandler,
    widgetResolver,
    localizationService,
    myActivitiesManager,
    locale
) {

    override fun getItems(widgetCollections: List<WidgetCollectionCellLayoutData>?): Flow<List<ActivitiesListItem>> =
        combine(
            myActivitiesManager.favoriteIdsFlow,
            getActivitiesForTags()
        ) { favoriteIds, activities ->
            val filteredActivities: MutableList<ActivitiesListItem> = activities.map {
                it.toListItem(localizationService, favoriteIds)
            }.sortedWith(
                compareBy<ActivitiesListItem.Card, Int?>(nullsLast()) { it.order }
                    .thenBy { it.name.lowercase(locale) }
            ).toMutableList()

            if (filteringHandler.currentMode == Mode.MY_FAVORITES) {
                filteredActivities.removeAll {
                    (it as? ActivitiesListItem.Card)?.let { item ->
                        !favoriteIds.contains(item.itemId)
                    } ?: false
                }
            }

            if (filteredActivities.size > 0 && widgetCollections != null) {
                var addWidgetCollections = 0
                getSortedWidgetItems(widgetCollections).forEach {
                    val indexToInsert = it.key + addWidgetCollections++
                    val widgetCollection = ActivitiesListItem.WidgetCollectionHolder(it.key, it.value)
                    if (filteredActivities.size > indexToInsert) {
                        filteredActivities.add(
                            indexToInsert,
                            widgetCollection
                        )
                    } else {
                        filteredActivities.add(widgetCollection)
                    }
                }
            }

            filteredActivities
        }

    private fun getActivitiesForTags() = filteringHandler.predicate.flatMapLatest { query ->
        activityRepository.getActivitiesForTags(query?.toSQL())
    }
}
