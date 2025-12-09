package com.greencopper.interfacekit.widgets.initializer

import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.fullwidthwidget.FullWidthImageWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.testmocks.interfacekit.MockRouteController
import com.greencopper.testmocks.interfacekit.MockWidgetParams
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class FullWidthImageWidgetInitializerTest {

    private lateinit var initializer: FullWidthImageWidgetInitializer
    private val routeController = MockRouteController()
    private val metrics = MockAggregateMetricsService()

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        initializer = FullWidthImageWidgetInitializer(routeController, metrics)
    }

    @Test
    fun resolveLayout_shouldGetLayout() {
        //given
        val context = ContextThemeWrapper(
            InstrumentationRegistry.getInstrumentation().targetContext,
            R.style.Base_Theme_MaterialComponents
        )

        //when
        val result = initializer.resolveLayout(context)

        //then
        assertThat(result).isInstanceOf(FullWidthImageWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val data = FullWidthImageWidgetParameters(
            FullWidthImageWidgetParameters.Image("image_light", "image_dark"),
            null,
            ItemNameAnalytics("analytics"),
            Route.Push(FeatureInfo(FeatureKey("fakeFeature", 1)))
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(FullWidthImageWidgetParameters::class.java)
        assertThat(result).isEqualTo(data)
    }

    @Test
    fun resolveParams_withoutParams_shouldThrow() {
        assertThrows<WidgetException.NoParametersProvided> {
            initializer.resolveParams(null)
        }
    }

    @Test
    fun resolveParams_withBadParams_shouldThrow() {
        assertThrows<WidgetException.ParametersDecodeFailed> {
            initializer.resolveParams(MockWidgetParams("name").encodeToJsonElement())
        }
    }

    @Test
    fun resolveGenerator_withBadParams_shouldThrow() {
        assertThrows<WidgetException.ParametersDecodeFailed> {
            initializer.resolveGenerator(
                MockWidgetParams("name").encodeToJsonElement(),
                "screenName123",
                mockk()
            )
        }
    }

    @Test
    fun resolveGenerator_withCorrectParams_shouldGetGenerator() {
        //given
        val data = FullWidthImageWidgetParameters(
            FullWidthImageWidgetParameters.Image("image_light", "image_dark"),
            null,
            ItemNameAnalytics("analytics"),
            Route.Push(FeatureInfo(FeatureKey("fakeFeature", 1)))
        )
        val screenName = "screenName123"
        val origin = mockk<Layout>()

        //when
        val result = initializer.resolveGenerator(
            data.encodeToJsonElement(),
            screenName,
            origin
        )

        //then
        result.topPadding shouldBe 0
        result.bottomPadding shouldBe 0
    }

    @Test
    fun serialize_deserialize_params() {
        testKiboSerializable(
            FullWidthImageWidgetParameters(
                FullWidthImageWidgetParameters.Image("image_light", "image_dark"),
                null,
                ItemNameAnalytics("analytics"),
                Route.Push(FeatureInfo(FeatureKey("fakeFeature", 1)))
            )
        )
    }
}
