package com.greencopper.interfacekit.widgets.initializer

import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.imagewithlabelwidget.ImageWithLabelWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class ImageWithLabelWidgetInitializerTest {

    private lateinit var initializer: ImageWithLabelWidgetInitializer

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        initializer = ImageWithLabelWidgetInitializer()
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
        assertThat(result).isInstanceOf(ImageWithLabelWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val data = ImageWithLabelWidgetParameters(
            "imageName",
            "title",
            "body",
            ImageWithLabelWidgetParameters.OnTap(
                "routeLink",
                ItemNameAnalytics("analytics"),
            )
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(ImageWithLabelWidgetParameters::class.java)
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
    fun resolveGenerator_shouldThrow() {
        assertThrows<NotImplementedError> {
            initializer.resolveGenerator(MockWidgetParams("name").encodeToJsonElement(), "screenName123", mockk())
        }
    }

    @Test
    fun serialize_deserialize_test() {
        testKiboSerializable(
            ImageWithLabelWidgetParameters(
                "imageName",
                "title",
                "body",
                ImageWithLabelWidgetParameters.OnTap(
                    "routeLink",
                    ItemNameAnalytics("analytics"),
                )
            )
        )
    }

}
