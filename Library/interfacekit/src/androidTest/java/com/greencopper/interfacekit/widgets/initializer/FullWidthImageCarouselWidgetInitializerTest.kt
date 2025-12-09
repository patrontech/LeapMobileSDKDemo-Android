package com.greencopper.interfacekit.widgets.initializer

import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.fullwidthimagecarousel.FullWidthImageCarouselWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class FullWidthImageCarouselWidgetInitializerTest {

    init {
        Toolkit.setupTest()
    }

    private val initializer = FullWidthImageCarouselWidgetInitializer(
        MockRouteController(),
        MockAggregateMetricsService(),
    )

    private val data = FullWidthImageCarouselWidgetParameters(
        images = listOf(
            FullWidthImageCarouselWidgetParameters.Image(
                imageName = "name",
                accessibilityLabel = "accessibilityLabel",
                onTap = FullWidthImageCarouselWidgetParameters.OnTap(
                    routeLink = "routeLink",
                    analytics = ItemNameAnalytics(""),
                )
            ),
            FullWidthImageCarouselWidgetParameters.Image(
                imageName = "name",
                accessibilityLabel = "accessibilityLabel",
                onTap = FullWidthImageCarouselWidgetParameters.OnTap(
                    routeLink = "routeLink",
                    analytics = ItemNameAnalytics(""),
                )
            ),
        ),
        ratio = 1.0f,
        accessibilityLabel = "accessibilityLabel",
    )

    @Test
    fun resolveLayout_shouldGetLayout() {
        //given
        bindSingleton<ColorRepository>(MockColorRepository())
        bindSingleton<TextStyleRepository>(MockTextStyleRepository())
        val context = ContextThemeWrapper(
            InstrumentationRegistry.getInstrumentation().targetContext,
            R.style.Base_Theme_MaterialComponents
        )

        //when
        val result = initializer.resolveLayout(context)

        //then
        assertThat(result).isInstanceOf(FullWidthImageCarouselWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(FullWidthImageCarouselWidgetParameters::class.java)
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
    fun resolveGenerator_shouldNotThrow() {
        //given
        val screenName = "fullwidthcarouselscreen"
        val origin = mockk<Layout>()

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
        testKiboSerializable(data)
    }
}
