package com.greencopper.interfacekit.tabBar.viewmodel

import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.tabBar.TabBarData
import com.greencopper.interfacekit.tabBar.TabBarLayoutData
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.shouldBe
import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.test.testReduce
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class TabBarReducerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    private val reducer = TabBarReducer(MockLocalizationService())

    override fun afterEach() {}

    @Test
    fun loadInitialTabs_returnsNewTabBarState() = runTest {
        val layoutData = TabBarLayoutData(
            defaultTabIndex = 0,
            trackMetadata = true,
            items = listOf(
                TabBarData.Item(
                    "",
                    "",
                    mockk(),
                    null,
                    ItemNameAnalytics(""),
                )
            ),
            redirectionHash = mockk(),
        )

        val oldState = TabBarState(-1, emptyList())
        val action = TabBarAction.LoadInitialTabData(layoutData)

        reducer.testReduce(oldState, action) { state, effect ->
            state.itemStates shouldBe listOf(TabItemState("", "", true, TabBarAction.TabSelected(0), ItemNameAnalytics("")))
            state.selectedIndex shouldBe action.data.defaultTabIndex
            effect shouldBe NoEffect
        }
    }

    @Test
    fun tabSelected_returnsSameState_withNewSelectedIndex() = runTest {
        val oldItems = listOf(
            TabItemState("0", "", true, TabBarAction.TabSelected(0), ItemNameAnalytics("")),
            TabItemState("1", "", false, TabBarAction.TabSelected(1), ItemNameAnalytics(""))
        )
        val oldState = TabBarState(0, oldItems)

        val action = TabBarAction.TabSelected(1)

        reducer.testReduce(oldState, action) { state, effect ->
            state.selectedIndex shouldBe action.index
            state.itemStates[0].isSelected shouldBe false
            state.itemStates[1].isSelected shouldBe true
            effect shouldBe NoEffect
        }
    }

    @Test
    fun tabRedirected_returnsSameState_withNewSelectedIndex() = runTest {
        val oldItems = listOf(
            TabItemState("0", "", true, TabBarAction.TabSelected(0), ItemNameAnalytics("")),
            TabItemState("1", "", false, TabBarAction.TabSelected(1), ItemNameAnalytics(""))
        )
        val oldState = TabBarState(0, oldItems)

        val action = TabBarAction.TabRedirected(1)

        reducer.testReduce(oldState, action) { state, effect ->
            state.selectedIndex shouldBe action.index
            state.itemStates[0].isSelected shouldBe false
            state.itemStates[1].isSelected shouldBe true
            effect shouldBe NoEffect
        }
    }
}
