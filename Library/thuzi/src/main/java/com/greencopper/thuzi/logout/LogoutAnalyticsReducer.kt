package com.greencopper.thuzi.logout

import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.MappedMetrics
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.thuzi.metrics.logout
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withoutEffect

internal class LogoutAnalyticsReducer(
    private val metricsService: AggregateMetricsService,
) : Reducer<LogoutState, LogoutAction> {

    override fun reduce(
        state: LogoutState,
        action: LogoutAction
    ): ReduceResult<LogoutState, LogoutAction> {
        return state.also {
            when (action) {
                is LogoutAction.LoadInitialState ->
                    metricsService.track(ScreenViewEvent(Screen.logout(action.data.analytics.screenName)))
                is LogoutAction.LogoutTapped ->
                    metricsService.track(LogoutTappedMetrics())
            }
        }.withoutEffect()
    }
}

internal class LogoutTappedMetrics : MappedMetrics {
    override fun track(provider: MappedProvider) {
        provider.track(EventName("logout/logout_click"), emptyMap())
    }
}
