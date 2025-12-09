package com.greencopper.interfacekit.tabBar.viewmodel

import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.tabBar.ui.TabBarTapEvent
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withoutEffect

internal class TabBarAnalyticsReducer(
    private val metricsService: AggregateMetricsService,
) : Reducer<TabBarState, TabBarAction> {

    override fun reduce(
        state: TabBarState,
        action: TabBarAction
    ): ReduceResult<TabBarState, TabBarAction> {
        when (action) {
            is TabBarAction.TabSelected -> {
                val item = state.itemStates[action.index]
                metricsService.track(TabBarTapEvent(item.analytics.itemName))
            }
            else -> { /* untracked action */ }
        }
        return state.withoutEffect()
    }
}
