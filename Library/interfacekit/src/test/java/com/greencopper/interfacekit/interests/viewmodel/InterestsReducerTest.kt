package com.greencopper.interfacekit.interests.viewmodel

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.interests.InterestsLayoutData
import com.greencopper.interfacekit.interests.recipe.Interest
import com.greencopper.interfacekit.interests.recipe.InterestsConfiguration
import com.greencopper.interfacekit.interests.recipe.InterestsConfigurationHolder
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.test.testReduce
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class InterestsReducerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val localStorage: LocalStorage = App.resolve()
    private val configHolder = InterestsConfigurationHolder()
    private val remoteStateDispatcher = MockRemoteStateDispatcher(json = App.resolve())

    private val reducer = InterestsReducer(
        localizationService = MockLocalizationService(),
        localStorage = localStorage,
        configHolder = configHolder,
        remoteStateDispatcher = remoteStateDispatcher,
        json = App.resolve(),
    )

    private val layoutData = InterestsLayoutData("title", "subtitle", ScreenNameAnalytics(""), null)

    override fun afterEach() {}

    @Test
    fun givenLayoutDataAndEmptyState_actionLoadInitialState_returnsEmptyStateWithLayoutData() = runTest {
        reducer.testReduce(InterestsState(), InterestsAction.LoadInitialState(layoutData)) { state, effect ->
            state.title shouldBe layoutData.title
            state.subtitle shouldBe layoutData.subtitle
            state.interests shouldBe emptyList()

            effect shouldBe NoEffect
        }
    }

    @Test
    fun givenSavedInterestsWithNoConfig_actionLoadInitialState_returnsEmptyStateWithLayoutData() = runTest {
        localStorage.project.interfaceKit.interestIds.value = setOf("1", "2")

        reducer.testReduce(InterestsState(), InterestsAction.LoadInitialState(layoutData)) { state, effect ->
            state.title shouldBe layoutData.title
            state.subtitle shouldBe layoutData.subtitle
            state.interests shouldBe emptyList()

            effect shouldBe NoEffect
        }
    }

    @Test
    fun givenSavedInterestsWithConfig_actionLoadInitialState_returnsStateWithLayoutData() = runTest {
        configHolder.currentConfiguration.value = InterestsConfiguration(listOf(
            Interest("2", "name", 2, "analyticsName2"),
            Interest("1", "name", 1, "analyticsName1"),
        ))
        localStorage.project.interfaceKit.interestIds.value = setOf("1")

        reducer.testReduce(InterestsState(), InterestsAction.LoadInitialState(layoutData)) { state, effect ->
            state.title shouldBe layoutData.title
            state.subtitle shouldBe layoutData.subtitle
            state.interests shouldBe listOf(
                InterestState("1", "name", true),
                InterestState("2", "name", false),
            )

            effect shouldBe NoEffect
        }
    }

    @Test
    fun givenUnsavedInterests_actionInterestTapped_returnsStateWithNewInterests() = runTest {
        configHolder.currentConfiguration.value = InterestsConfiguration(listOf(
            Interest("2", "name", 2, "analyticsName2"),
            Interest("1", "name", 1, "analyticsName1"),
        ))
        localStorage.project.interfaceKit.interestIds.value = setOf("1")

        reducer.testReduce(InterestsState(), InterestsAction.InterestTapped("2", false)) { state, effect ->
            state.interests shouldBe listOf(
                InterestState("1", "name", true),
                InterestState("2", "name", true),
            )

            effect shouldBe NoEffect
            remoteStateDispatcher.dispatchCallCount shouldBe 1
        }
    }

    @Test
    fun givenAllSavedInterests_actionInterestTapped_returnsStateWithLessInterests() = runTest {
        configHolder.currentConfiguration.value = InterestsConfiguration(listOf(
            Interest("2", "name", 2, "analyticsName2"),
            Interest("1", "name", 1, "analyticsName1"),
        ))
        localStorage.project.interfaceKit.interestIds.value = setOf("1", "2")

        reducer.testReduce(InterestsState(), InterestsAction.InterestTapped("1", true)) { state, effect ->
            state.interests shouldBe listOf(
                InterestState("1", "name", false),
                InterestState("2", "name", true),
            )

            effect shouldBe NoEffect
            remoteStateDispatcher.dispatchCallCount shouldBe 1
        }
    }
}
