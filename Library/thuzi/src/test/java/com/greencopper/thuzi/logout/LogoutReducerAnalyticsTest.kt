package com.greencopper.thuzi.logout

import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.thuzi.metrics.logout
import com.toggl.komposable.test.testReduceNoOp
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LogoutReducerAnalyticsTest : CoroutineTest() {

    private val metricsService = MockAggregateMetricsService()
    private val layoutData = LogoutLayoutData(ScreenNameAnalytics("logoutScreen"))

    private val reducer = LogoutAnalyticsReducer(metricsService)

    override fun afterEach() { }

    @Test
    fun screenLoaded_tracksScreenView() = runTest {
        reducer.testReduceNoOp(LogoutState(), LogoutAction.LoadInitialState(layoutData))
        assertThat(metricsService.trackedMetrics.last()).usingRecursiveComparison()
            .isEqualTo(ScreenViewEvent(Screen.logout(layoutData.analytics.screenName)))
    }

    @Test
    fun logoutTapped_tracksEvent() = runTest {
        reducer.testReduceNoOp(LogoutState(), LogoutAction.LogoutTapped)
        assertThat(metricsService.trackedMetrics.last()).usingRecursiveComparison()
            .isEqualTo(LogoutTappedMetrics())
    }
}
