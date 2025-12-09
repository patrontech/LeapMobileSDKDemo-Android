package com.greencopper.interfacekit.tabBar

import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.sample.SampleData
import com.greencopper.interfacekit.tabBar.ui.TabBarFragment
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.testmocks.mockBundleConstructor
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class TabBarInitializerTest {

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
        mockBundleConstructor()
    }

    private lateinit var tabBarInitializer: TabBarInitializer

    @BeforeEach
    fun setupEach() {
        tabBarInitializer = TabBarInitializer()
    }

    @Test
    fun getLayout_withoutParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            tabBarInitializer.getLayout(null)
        }
    }

    @Test
    fun getLayout_withWrongParams_shouldThrow() {
        val params = buildJsonObject { put("testKey", "testValue") }
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            tabBarInitializer.getLayout(params)
        }
    }

    @Test
    fun getLayout_withCorrectParams_shouldSucceed() {
        val json: Json = App.resolve()
        val params = json.encodeToJsonElement(
            TabBarData.serializer(),
            TabBarData(
                selectedIndex = 0,
                trackMetadata = false,
                items = listOf(
                    TabBarData.Item(
                        name = "Sample",
                        iconName = "placeholder.png",
                        display = TabBarData.Display.Embedded(
                            feature = FeatureInfo(
                                FeatureKey(
                                    "InterfaceKit.Sample",
                                    1
                                ),
                                json.encodeToJsonElement(
                                    SampleData(
                                        "TestSample",
                                        "placeholder.png"
                                    )
                                ).jsonObject
                            )
                        ),
                        analytics = ItemNameAnalytics("TestItem")
                    ),
                    TabBarData.Item(
                        name = "FakeFeature",
                        iconName = "placeholder.png",
                        display = TabBarData.Display.Routing(
                            route = Route.Present(
                                feature = FeatureInfo(
                                    FeatureKey(
                                        "Test.Feature",
                                        1
                                    ),
                                    null
                                )
                            )
                        ),
                        analytics = ItemNameAnalytics("TestItem")
                    )
                )
            )
        )
        val layout = assertDoesNotThrow {
            tabBarInitializer.getLayout(params)
        }
        assertThat(layout).isInstanceOf(TabBarFragment::class.java)
    }
}
