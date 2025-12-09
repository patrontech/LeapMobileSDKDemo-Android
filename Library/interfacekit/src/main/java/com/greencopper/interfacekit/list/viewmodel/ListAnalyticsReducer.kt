package com.greencopper.interfacekit.list.viewmodel

import com.greencopper.core.metrics.Metrics
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.list.initializer.ListLayoutData
import com.greencopper.interfacekit.list.provider.EventFavoritesListAnalytics
import com.greencopper.interfacekit.list.provider.ScreenListEvent
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withoutEffect

internal class ListAnalyticsReducer(
    private val metricsService: AggregateMetricsService,
    private val listData: ListLayoutData,
) : Reducer<ListState, ListAction> {

    private var lastTrackedScreenEvent: Metrics? = null

    override fun reduce(state: ListState, action: ListAction): ReduceResult<ListState, ListAction> {
        return state.also { state ->
            when (action) {
                is ListAction.User.TappedAddToMyFavorites -> {
                    metricsService.track(
                        EventFavoritesListAnalytics(
                            EventName(listData.analytics.addToMyFavoritesEventName),
                            screenName = listData.analytics.screenName,
                            itemId = action.listItemId.toString(),
                            itemName = action.itemName
                        )
                    )
                }

                is ListAction.User.TappedRemoveFromMyFavorites -> {
                    metricsService.track(
                        EventFavoritesListAnalytics(
                            EventName(listData.analytics.removeFromMyFavoritesEventName),
                            screenName = listData.analytics.screenName,
                            itemId = action.listItemId.toString(),
                            itemName = action.itemName
                        )
                    )
                }

                is ListAction.ItemsReloaded -> {
                    val filteringPredicate = state.filteringPredicate.toString()
                    ScreenListEvent(
                        name = listData.analytics.screenName,
                        klass = listData.analytics.screenClass,
                        listMode = listData.mode,
                        favoritesOnly = state.isInMyFavorites,
                        filteringPredicate = filteringPredicate
                    ).takeIf {
                        it != lastTrackedScreenEvent
                    }?.let {
                        lastTrackedScreenEvent = it
                        metricsService.track(
                            it
                        )
                    }
                }

                else -> Unit
            }
        }.withoutEffect()
    }
}
