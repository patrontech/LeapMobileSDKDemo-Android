package com.greencopper.interfacekit.widgets.initializer

import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.bannerwidget.BannerWidgetLayout
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.testmocks.interfacekit.MockColorRepository
import com.greencopper.testmocks.interfacekit.MockRouteController
import com.greencopper.testmocks.interfacekit.MockTextStyleRepository
import com.greencopper.testmocks.interfacekit.MockWidgetParams
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

//<editor-fold desc="tomorrow"> ///////////////////////
/* test BannerWidget in regular widgetCollection (not Compose) */
//</editor-fold>/ END:  ///////////////////////
internal class BannerWidgetInitializerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var initializer: BannerWidgetInitializer
    private val routeController = MockRouteController()
    private val metrics = MockAggregateMetricsService()

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        initializer = BannerWidgetInitializer(
            routeController,
            metrics,
        )
    }

    @Test
    fun resolveLayout_shouldGetLayout() {
        //given
        bindSingleton<ColorRepository>(MockColorRepository())
        bindSingleton<TextStyleRepository>(MockTextStyleRepository())
        val context = ContextThemeWrapper(
            context,
            R.style.Base_Theme_MaterialComponents
        )

        //when
        val result = initializer.resolveLayout(context)

        //then
        assertThat(result).isInstanceOf(BannerWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val routeLink = "presentRoute"

        val data = BannerWidgetParameters(
            "title",
            "subtitle",
            BannerWidgetParameters.Button(
                "text",
                "iconName",
                BannerWidgetParameters.OnTap(routeLink, ItemNameAnalytics("analytics"))
            ),
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(BannerWidgetParameters::class.java)
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
        val data = BannerWidgetParameters(
            "title",
            "subtitle",
            BannerWidgetParameters.Button(
                "text",
                "iconName",
                BannerWidgetParameters.OnTap("routeLink", ItemNameAnalytics("analytics"))
            ),
        )
        val screenName = "screenName123"
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
        testKiboSerializable(
            BannerWidgetParameters(
                "title",
                "subtitle",
                BannerWidgetParameters.Button(
                    "text",
                    "iconName",
                    BannerWidgetParameters.OnTap("routeLink", ItemNameAnalytics("analytics"))
                ),
            )
        )
    }

}
