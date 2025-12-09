package com.greencopper.interfacekit.widgets

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.TestLocalStorageContainer
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance
import com.greencopper.interfacekit.widgets.resolver.WidgetCollectionResolver
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class WidgetCollectionInitializerTest {

    private lateinit var widgetCollectionInitializer: WidgetCollectionInitializer
    private val widgetCollectionConfigHolder = WidgetCollectionConfigurationHolder()
    private val localStorageContainer = TestLocalStorageContainer()
    private val localStorage = LocalStorage(
        project = "testProject",
        localStorageContainer = localStorageContainer
    )

    init {
        Toolkit.setupTest()
        bindSingleton(widgetCollectionConfigHolder)
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    @BeforeEach
    fun setupEach() {
        widgetCollectionInitializer =
            WidgetCollectionInitializer(WidgetCollectionResolver(widgetCollectionConfigHolder))
    }

    @Test
    fun getLayout_withNullParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            widgetCollectionInitializer.getLayout(null)
        }
    }

    @Test
    fun getLayout_withWrongParams_shouldThrow() {
        val params = JsonNull
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            widgetCollectionInitializer.getLayout(params)
        }
    }

    @Test
    fun getLayout_withNonExistingCollection_shouldThrow() {
        widgetCollectionConfigHolder.currentConfiguration.value = null
        val widgetCollectionData = WidgetCollectionData(
            name = "collection5",
            analytics = ScreenNameAnalytics("textWidgetCollection")
        )
        val jsonParams = widgetCollectionData.encodeToJsonElement()
        assertThrows<IllegalArgumentException> {
            widgetCollectionInitializer.getLayout(jsonParams)
        }
    }

    @Test
    fun getLayout_withExistingCollection_shouldReturn() {
        mockBundleConstructor()
        widgetCollectionConfigHolder.currentConfiguration.value = WidgetCollectionConfiguration(
            mapOf(
                "collection5" to Instance(
                    header = Instance.HeaderInfo(
                        imageName = "image",
                        ratio = 1.3f,
                        cornerRadius = 10,
                        shadow = false,
                    ),
                    widgets = listOf(
                        Instance.WidgetInfo(
                            Instance.WidgetKey("TestWidget", 1),
                            JsonObject(mapOf())
                        )
                    )
                )
            )
        )
        assertThat(widgetCollectionConfigHolder.currentConfiguration.value).isNotNull
        val widgetCollectionData = WidgetCollectionData(
            name = "collection5",
            analytics = ScreenNameAnalytics("textWidgetCollection")
        )
        val jsonParams = widgetCollectionData.encodeToJsonElement()
        val layout = widgetCollectionInitializer.getLayout(jsonParams)
        assertThat(layout).isNotNull
    }

    @Test
    fun getRedirectionHash_withoutParams_shouldReturnDefault() {
        val hash = widgetCollectionInitializer.redirectionHashFor(null)
        assertThat(hash).isEqualTo(RedirectionHash(WidgetCollectionInitializer.key))
    }

    @Test
    fun getRedirectionHash_withWrongParams_shouldReturnDefault() {
        val params = JsonNull
        val hash = widgetCollectionInitializer.redirectionHashFor(params)
        assertThat(hash).isEqualTo(RedirectionHash(WidgetCollectionInitializer.key))
    }

    @Test
    fun getRedirectionHash_withCorrectParams_shouldSucceed() {
        val params = App.resolve<Json>().encodeToJsonElement(
            WidgetCollectionData.serializer(),
            WidgetCollectionData(
                name = "https://greencopper.com",
                analytics = ScreenNameAnalytics("textWidgetCollection")
            )
        )
        val hash = widgetCollectionInitializer.redirectionHashFor(params)
        assertThat(hash).isNotNull
    }
}
