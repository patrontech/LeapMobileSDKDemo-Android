package com.greencopper.interfacekit.widgets.initializer

import android.content.res.Resources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.core.metrics.ItemNameIdAnalytics
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.cardadwidget.CardAdWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class CardAdWidgetInitializerTest {

    private lateinit var initializer: CardAdWidgetInitializer
    private val imageService = MockImageService()
    private val routeController = MockRouteController()
    private val metricsService = MockAggregateMetricsService()

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        initializer = CardAdWidgetInitializer(routeController, metricsService, imageService)
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
        assertThat(result).isInstanceOf(CardAdWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        imageService.isImageAvailable_result = true

        val ad1 = CardAdWidgetParameters.Ad(
            image = "image1",
            weight = 50,
            accessibilityLabel = "label",
            onTapRouteLink = "routeLink1",
            analytics = ItemNameIdAnalytics(
                itemName = "itemName1",
                itemId = "itemId2",
            )
        )
        val data = CardAdWidgetParameters(listOf(ad1))

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(CardAdWidgetLayoutParameters::class.java)
        result as CardAdWidgetLayoutParameters
        assertThat(result.imageName).isEqualTo(data.ads.first().image)
    }

    @Test
    fun resolveParams_withCorrectParams_withoutValidImage_shouldThrow() {
        //given
        val ad1 = CardAdWidgetParameters.Ad(
            image = "image1",
            weight = 50,
            accessibilityLabel = "label",
            onTapRouteLink = "routeLink1",
            analytics = ItemNameIdAnalytics(
                itemName = "itemName1",
                itemId = "itemId2",
            )
        )
        val data = CardAdWidgetParameters(listOf(ad1))

        //then
        assertThrows<WidgetException.InvalidParametersProvided> {
            initializer.resolveParams(data.encodeToJsonElement())
        }
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
    fun resolveGenerator_withCorrectParams_shouldNotThrow() {
        //given
        imageService.isImageAvailable_result = true
        val ad1 = CardAdWidgetParameters.Ad(
            image = "image1",
            weight = 50,
            accessibilityLabel = "label",
            onTapRouteLink = "routeLink1",
            analytics = ItemNameIdAnalytics(
                itemName = "itemName1",
                itemId = "itemId2",
            )
        )
        val data = CardAdWidgetParameters(listOf(ad1))
        val screenName = "screenName123"
        val origin = mockk<Layout>()
        val resources = mockk<Resources>()
        every { origin.resources } returns resources
        every { resources.getInteger(R.integer.ad_widget_min_margin) } returns 123

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
        testKiboSerializable(CardAdWidgetParameters(listOf(
            CardAdWidgetParameters.Ad(
                image = "image1",
                weight = 50,
                accessibilityLabel = "label",
                onTapRouteLink = "routeLink1",
                analytics = ItemNameIdAnalytics(
                    itemName = "itemName1",
                    itemId = "itemId2",
                )
            ))))
        testKiboSerializable(
            CardAdWidgetLayoutParameters(
                "image1",
                "routeLink1",
                accessibilityLabel = "label",
                ItemNameIdAnalytics("itemName1", "itemId1")
            ))
    }

}
