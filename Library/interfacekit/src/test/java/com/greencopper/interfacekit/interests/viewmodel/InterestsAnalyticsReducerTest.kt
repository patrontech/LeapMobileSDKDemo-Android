package com.greencopper.interfacekit.interests.viewmodel

import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.interfacekit.interests.InterestSelected
import com.greencopper.interfacekit.interests.InterestUnselected
import com.greencopper.interfacekit.interests.InterestsLayoutData
import com.greencopper.interfacekit.interests.InterestsPickerClosed
import com.greencopper.interfacekit.interests.recipe.Interest
import com.greencopper.interfacekit.interests.recipe.InterestsConfiguration
import com.greencopper.interfacekit.interests.recipe.InterestsConfigurationHolder
import com.greencopper.interfacekit.metrics.interestsPicker
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import com.toggl.komposable.test.testReduceNoOp
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class InterestsAnalyticsReducerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val metricsService = MockAggregateMetricsService()
    private val layoutData = InterestsLayoutData("", null, ScreenNameAnalytics("interestsScreen"), null)
    private val viewState = InterestsState("title", "subtitle", "buttonTitle", emptyList())
    private val configHolder = InterestsConfigurationHolder().apply {
        currentConfiguration.value = InterestsConfiguration(listOf(
            Interest("1", "name", 1, "analyticsName1")
        ))
    }

    private val reducer = InterestsAnalyticsReducer(
        metricsService = metricsService,
        localizationService = MockLocalizationService(),
        configHolder = configHolder,
        localStorage = App.resolve(),
        layoutData = layoutData,
    )

    override fun afterEach() {}

    @Test
    fun screenLoaded_tracksScreenView() = runTest {
        reducer.testReduceNoOp(viewState, InterestsAction.LoadInitialState(layoutData))
        assertThat(metricsService.trackedMetrics.last()).usingRecursiveComparison()
            .isEqualTo(ScreenViewEvent(Screen.interestsPicker(layoutData.analytics.screenName)))
    }

    @Test
    fun unselectedInterestTapped_tracksInterestSelected() = runTest {
        val action = InterestsAction.InterestTapped("1", false)
        reducer.testReduceNoOp(viewState, action)

        assertThat(metricsService.trackedMetrics.last()).usingRecursiveComparison()
            .isEqualTo(
                InterestSelected(
                    "analyticsName1",
                    action.id,
                    layoutData.analytics.screenName
                )
            )
    }

    @Test
    fun selectedInterestTapped_tracksInterestUnselected() = runTest {
        val action = InterestsAction.InterestTapped("1", true)
        reducer.testReduceNoOp(viewState, action)

        assertThat(metricsService.trackedMetrics.last()).usingRecursiveComparison()
            .isEqualTo(
                InterestSelected(
                    "analyticsName1",
                    action.id,
                    layoutData.analytics.screenName
                )
            )
    }

    @Test
    fun interestPickerClosed_tracksPickerClosed() = runTest {
        reducer.testReduceNoOp(viewState, InterestsAction.InterestsClosed)
        metricsService.wasMetricTracked(InterestsPickerClosed::class) shouldBe true
    }

    @Test
    fun interestPickerLoaded_doesNotTrack() = runTest {
        reducer.testReduceNoOp(viewState, InterestsAction.LoadInitialState(layoutData))

        metricsService.wasMetricTracked(InterestSelected::class) shouldBe false
        metricsService.wasMetricTracked(InterestUnselected::class) shouldBe false
        metricsService.wasMetricTracked(InterestsPickerClosed::class) shouldBe false
    }
}
