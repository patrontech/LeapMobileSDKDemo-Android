package com.greencopper.maps.locationlist

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
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class LocationListInitializerTest {

    private val initializer = LocationListInitializer()
    private val json: Json = App.resolve()

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

        val parameters = LocationListData(
            analytics = ScreenNameAnalytics("TestScreen"),
            title = "title",
            displayImages = true,
            filtering = null,
            search = LocationListData.Search("routeLink"),
            onLocationTap = "test",
            collections = listOf(
                WidgetCollectionData(
                    3,
                    WidgetCollectionConfiguration.Instance(null, listOf(widgetInfo))
                )
            ),
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

    @Test
    fun whenGettingRedirectionHash_withParams_shouldGetHash() {
        val param = LocationListData(
            ScreenNameAnalytics("TestScreen"),
            "title",
            true,
            null,
            LocationListData.Search("routeLink"),
            "test"
        )
        val jsonString = json.encodeToString(param)
        val paramJson = json.parseToJsonElement(jsonString)
        val redirectionHash = initializer.redirectionHashFor(paramJson)
        assertThat(redirectionHash).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withParams_InvalidJson_shouldGetHash() {
        val jsonString = "{\"test\": \"test\"}"
        val paramJson = json.parseToJsonElement(jsonString)
        val redirectionHash = initializer.redirectionHashFor(paramJson)
        assertThat(redirectionHash).isNotNull
    }

    @Test
    fun whenGettingLayout_withCorrectParamsAndNullSearch_shouldGetLayout() {
        mockBundleConstructor()
        val parameters = LocationListData(
            ScreenNameAnalytics("TestScreen"),
            "title",
            false,
            null,
            null,
            "test"
        )
        val layout = initializer.getLayout(
            parameters.encodeToJsonElement()
        )
        assertThat(layout).isNotNull
    }
}
