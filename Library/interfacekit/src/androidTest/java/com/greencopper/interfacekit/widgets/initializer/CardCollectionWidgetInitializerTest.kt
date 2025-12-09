package com.greencopper.interfacekit.widgets.initializer

import android.content.res.Resources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.cardcollectionwidget.CardCollectionWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class CardCollectionWidgetInitializerTest {

    init {
        Toolkit.setupTest()
    }

    private val initializer = CardCollectionWidgetInitializer(
        MockRouteController(),
        MockAggregateMetricsService(),
    )

    private val data = CardCollectionWidgetParameters(
        "title",
        listOf(
            CardCollectionWidgetParameters.Item(
                style = CardCollectionWidgetParameters.Style(CardCollectionItemStyle.icon, null, "icon"),
                label = "label",
                accessibilityLabel = "accessibilityLabel",
                onTap = "routeLink",
                analytics = CardCollectionWidgetParameters.Analytics("", null)
            ),
            CardCollectionWidgetParameters.Item(
                style = CardCollectionWidgetParameters.Style(CardCollectionItemStyle.image, "image", null),
                label = "label",
                accessibilityLabel = "accessibilityLabel",
                onTap = "routeLink",
                analytics = CardCollectionWidgetParameters.Analytics("", "")
            ),
        )
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
        assertThat(result).isInstanceOf(CardCollectionWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(CardCollectionWidgetParameters::class.java)
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
        val screenName = "cardcollectionscreenname"
        val origin = mockk<Layout>()
        val resources = mockk<Resources>()
        every { origin.resources } returns resources
        every { resources.getDimension(R.dimen.card_collection_content_padding) } returns 8f
        every { resources.getDimension(R.dimen.card_collection_vertical_padding) } returns 8f
        every { resources.getDimension(R.dimen.card_collection_item_spacing) } returns 8f
        every { resources.getInteger(R.integer.card_collection_vertical_padding) } returns 1

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
