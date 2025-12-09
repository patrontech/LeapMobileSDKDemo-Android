package com.greencopper.event.activity

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionData
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class ActivitiesListInitializerTest {

    private val initializer = ActivitiesListInitializer()

    @BeforeEach
    fun setUp() {
        Toolkit.setupTest()
        bindProvider<LocalizationService>(MockLocalizationService())
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    @Test
    fun whenGettingLayout_withoutParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenGettingLayout_withWrongParams_shouldThrow() {
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(buildJsonObject { put("testKey", "testValue") })
        }
    }

    @Test
    fun whenGettingLayout_withCorrectParams_shouldGetLayout() {
        mockBundleConstructor()
        val widgetInfo: WidgetCollectionConfiguration.Instance.WidgetInfo = KiboSerializable.decodeFromString("""
                            {
                              "key": {
                                "name": "InterfaceKit.Widget.Image",
                                "version": 1
                              },
                              "params": {
                                "imageName": "imagewidget_image_20210323055120_185a99cc.png",
                                "analytics": {
                                  "itemName": "EventPass"
                                }
                              }
                            }
                    """.trimIndent()
        )

        val parameters = ActivitiesListData(
            "title",
            "kibo://activityList",
            null,
            ActivitiesListData.Search("routeLink"),
            true,
            listOf(
                WidgetCollectionData(
                    3,
                    WidgetCollectionConfiguration.Instance(null, listOf(widgetInfo))
                )
            ),
            null,
            null,
            ScreenNameAnalytics("TestScreen"),
        )
        val layout = initializer.getLayout(
            parameters.encodeToJsonElement()
        )
        assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetHash() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash).isNotNull
    }
}
