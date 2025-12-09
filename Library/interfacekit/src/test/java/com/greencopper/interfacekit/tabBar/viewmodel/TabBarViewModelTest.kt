package com.greencopper.interfacekit.tabBar.viewmodel

import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.service.MappedMetadataService
import com.greencopper.interfacekit.metrics.tabBarCurrentTab
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.tabBar.TabBarData
import com.greencopper.interfacekit.tabBar.TabBarLayoutData
import com.greencopper.testmocks.MockStore
import com.greencopper.testmocks.interfacekit.MockFeatureResolver
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TabBarViewModelTest {

    private val store = MockStore<TabBarState, TabBarAction>(TabBarState(0, listOf()))
    private val mockLayout: Layout = mockk(relaxed = true)
    private val featureResolver = MockFeatureResolver()
    private val mappedMetadataService = MappedMetadataService()

    private val viewModel = TabBarViewModel(
        viewBuilder = mockk(),
        store,
        featureResolver,
        mappedMetadataService,
    )

    init {
        Toolkit.setupTest()
    }

    @Test
    fun getFragmentLayout_whenError() {
        val featureInfo = FeatureInfo(FeatureKey("name", 1))
        assertThat(viewModel.getFragmentLayout(featureInfo)).isNull()
    }

    @Test
    fun getFragmentLayout_whenSuccess() {
        val featureInfo = FeatureInfo(FeatureKey("name", 1))
        featureResolver.layout = mockLayout
        assertThat(viewModel.getFragmentLayout(featureInfo)).isEqualTo(mockLayout)
    }

    @Test
    fun retainCurrentTabName() {
        val name = "test"
        viewModel.retainCurrentTabName(name)
        assertThat(mappedMetadataService[EventParameter.tabBarCurrentTab]).isEqualTo(name)
    }

    @Test
    fun getAvailableLayouts_returnsLayoutForEachTabItem() {
        featureResolver.layout = mockLayout

        val data = TabBarLayoutData(0, false, listOf(
            TabBarData.Item("", "",
                TabBarData.Display.Routing(Route.Push(FeatureInfo(FeatureKey("", 1)))),
                null, ItemNameAnalytics("")),
            TabBarData.Item("", "",
                TabBarData.Display.Routing(Route.Present(FeatureInfo(FeatureKey("", 1)))),
                null, ItemNameAnalytics("")),
        ), RedirectionHash(FeatureKey("", 1)))

        viewModel.getAvailableLayouts(data).size shouldBe 2
    }
}
