package com.greencopper.interfacekit.tabBar.viewmodel

import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.tabBar.ui.TabBarTapEvent
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.testmocks.shouldBe
import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.test.testReduce
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class TabBarAnalyticsReducerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    private val metricsService = MockAggregateMetricsService()

    private val reducer = TabBarAnalyticsReducer(metricsService)

    private val itemName = "testAnalytics"
    private val tabItemState = TabItemState("", "", false, TabBarAction.TabSelected(0), ItemNameAnalytics(itemName))
    private val tabBarState = TabBarState(
        0,
        listOf(tabItemState),
    )

    override fun afterEach() { }

    @Test
    fun tabSelected_tracksTabName() = runTest {
        reducer.testReduce(tabBarState, TabBarAction.TabSelected(0)) { state, effect ->
            metricsService.trackedMetrics.last() shouldBe TabBarTapEvent(itemName)
            effect shouldBe NoEffect
        }
    }

    @Test
    fun loadAvailableTabs_tracksNothing() = runTest {
        reducer.testReduce(tabBarState, TabBarAction.LoadInitialTabData(mockk())) { state, effect ->
            metricsService.trackedMetrics shouldBe emptyList()
            effect shouldBe NoEffect
        }
    }

    @Test
    fun tabRedirected_tracksNothing() = runTest {
        reducer.testReduce(tabBarState, TabBarAction.TabRedirected(0)) { state, effect ->
            metricsService.trackedMetrics shouldBe emptyList()
            effect shouldBe NoEffect
        }
    }
}
