package com.greencopper.interfacekit.widgets.initializer

import android.content.res.Resources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.imagewidget.ImageWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class ImageWidgetInitializerTest {

    private lateinit var initializer: ImageWidgetInitializer
    private val routeController = MockRouteController()
    private val metricsService = MockAggregateMetricsService()

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        initializer = ImageWidgetInitializer(routeController, metricsService)
    }

    @Test
    fun resolveLayout_shouldGetLayout() {
        //given
        val context = ContextThemeWrapper(
            InstrumentationRegistry.getInstrumentation().targetContext,
            R.style.Base_Theme_MaterialComponents
        )
        bindSingleton<ColorRepository>(MockColorRepository())
        bindSingleton<TextStyleRepository>(MockTextStyleRepository())

        //when
        val result = initializer.resolveLayout(context)

        //then
        assertThat(result).isInstanceOf(ImageWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val data = ImageWidgetParameters(
            ImageWidgetParameters.Image("image_light", "image_dark"),
            null,
            ItemNameAnalytics("analytics"),
            Route.Push(FeatureInfo(FeatureKey("fakeFeature", 1)))
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(ImageWidgetParameters::class.java)
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
    fun resolveGenerator_withCorrectParams_shouldGetGenerator() {
        //given
        val data = ImageWidgetParameters(
            ImageWidgetParameters.Image("image_light", "image_dark"),
            null,
            ItemNameAnalytics("analytics"),
            Route.Push(FeatureInfo(FeatureKey("fakeFeature", 1)))
        )
        val screenName = "screenName123"
        val origin = mockk<Layout>()
        val resources = mockk<Resources>()
        every { origin.resources } returns resources
        every { resources.getInteger(R.integer.widget_min_margin) } returns 123

        //then
        assertDoesNotThrow {
            initializer.resolveGenerator(
                data.encodeToJsonElement(),
                screenName,
                origin
            )
        }
    }

    @Test
    fun serialize_deserialize_test() {
        testKiboSerializable(
            ImageWidgetParameters(
                ImageWidgetParameters.Image("image_light", "image_dark"),
                "null",
                ItemNameAnalytics("analytics"),
                Route.Push(FeatureInfo(FeatureKey("fakeFeature", 1)))
            )
        )
    }

}
