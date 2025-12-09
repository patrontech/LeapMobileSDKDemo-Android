package com.greencopper.interfacekit.list.viewmodel

import com.greencopper.core.metrics.Metrics
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.interfacekit.filtering.MockFilteringPredicateComputed
import com.greencopper.interfacekit.list.initializer.ListMode
import com.greencopper.interfacekit.list.provider.EventFavoritesListAnalytics
import com.greencopper.interfacekit.list.provider.ScreenListEvent
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.toolkit.Toolkit
import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.test.testReduce
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ListAnalyticsReducerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private var initialState = ListState()
    private var initialData = createListData()

    private val metricsService = MockAggregateMetricsService()

    private val reducer: ListAnalyticsReducer by lazy {
        spyk(
            ListAnalyticsReducer(
                metricsService = metricsService,
                listData = initialData,
            ),
            recordPrivateCalls = true,
        )
    }

    override fun afterEach() {
        unmockkAll()
    }

    @Test
    fun tappedAddToMyFavorites() {
        testFunction(
            ListAction.User.TappedAddToMyFavorites(123L, "analyticsName"),
            EventFavoritesListAnalytics(
                EventName(initialData.analytics.addToMyFavoritesEventName),
                screenName = initialData.analytics.screenName,
                itemId = "123",
                itemName = "analyticsName"
            )
        )
    }

    @Test
    fun tappedRemoveFromMyFavorites() {
        testFunction(
            ListAction.User.TappedRemoveFromMyFavorites(123L, "analyticsName"),
            EventFavoritesListAnalytics(
                EventName(initialData.analytics.removeFromMyFavoritesEventName),
                screenName = initialData.analytics.screenName,
                itemId = "123",
                itemName = "analyticsName"
            )
        )
    }

    @Test
    fun reloadedItems() {
        initialData = initialData.copy(mode = ListMode.Grid(1))

        testFunction(
            ListAction.ItemsReloaded,
            ScreenListEvent(
                name = initialData.analytics.screenName,
                klass = initialData.analytics.screenClass,
                listMode = ListMode.Grid(1),
                favoritesOnly = initialState.isInMyFavorites,
                filteringPredicate = initialState.filteringPredicate.toString()
            )
        )

        metricsService.trackedMetrics.clear()
        initialState = initialState.copy(filteringPredicate = MockFilteringPredicateComputed("testing"))

        testFunction(
            ListAction.ItemsReloaded,
            ScreenListEvent(
                name = initialData.analytics.screenName,
                klass = initialData.analytics.screenClass,
                listMode = ListMode.Grid(1),
                favoritesOnly = initialState.isInMyFavorites,
                filteringPredicate = initialState.filteringPredicate.toString()
            )
        )
    }

    @Test
    fun `reloadedItems event shouldn't send same event`() = runTest {
        initialData = initialData.copy(mode = ListMode.Grid(1))

        reducer.testReduce(initialState, ListAction.ItemsReloaded) { state, effect ->
            state shouldBe initialState

            metricsService.trackedMetrics shouldBe listOf(
                ScreenListEvent(
                    name = initialData.analytics.screenName,
                    klass = initialData.analytics.screenClass,
                    listMode = ListMode.Grid(1),
                    favoritesOnly = initialState.isInMyFavorites,
                    filteringPredicate = initialState.filteringPredicate.toString()
                )
            )
            effect shouldBe NoEffect
        }

        metricsService.trackedMetrics.clear()

        reducer.testReduce(initialState, ListAction.ItemsReloaded) { state, effect ->
            state shouldBe initialState

            metricsService.trackedMetrics shouldBe emptyList()
            effect shouldBe NoEffect
        }
    }

    @Test
    fun `unknown action shouldn't do anything`() = runTest {
        reducer.testReduce(initialState, ListAction.ScreenLoaded(
            mockk(),
            ListReducer.UiClient {}
        )) { state, effect ->
            state shouldBe initialState

            assertThat(metricsService.trackedMetrics).isEmpty()
            effect shouldBe NoEffect
        }
    }

    private fun testFunction(action: ListAction, expectedMetrics: Metrics) = runTest {
        reducer.testReduce(initialState, action) { state, effect ->
            state shouldBe initialState

            assertThat(metricsService.trackedMetrics).hasSize(1)
            assertThat(metricsService.trackedMetrics.first()).usingRecursiveComparison()
                .isEqualTo(expectedMetrics)

            effect shouldBe NoEffect
        }
    }
}
