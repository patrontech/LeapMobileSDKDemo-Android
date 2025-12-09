package com.greencopper.interfacekit.widgets.initializer

import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.Color
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.buttonwidget.ButtonWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class ButtonWidgetInitializerTest {

    private lateinit var initializer: ButtonWidgetInitializer

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        initializer = ButtonWidgetInitializer()
    }

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
        assertThat(result).isInstanceOf(ButtonWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val data = ButtonWidgetParameters(
            "iconName",
            "text",
            ButtonWidgetColors(
                Color(0x123456, 0x4b3d2a),
                Color(0x123456, 0x4b3d2a),
                Color(0x123456, 0x4b3d2a),
                Color(0x123456, 0x4b3d2a),
            ),
            ItemNameAnalytics("analytics"),
            Route.Push(FeatureInfo(FeatureKey("fakeFeature", 1)))
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(ButtonWidgetParameters::class.java)
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
    public fun resolveGenerator_shouldThrow() {
        assertThrows<NotImplementedError> {
            initializer.resolveGenerator(MockWidgetParams("name").encodeToJsonElement(), "screenName123", mockk())
        }
    }

    @Test
    fun serialize_deserialize_test() {
        testKiboSerializable(
            ButtonWidgetParameters(
                "iconName",
                "text",
                ButtonWidgetColors(
                    Color(0x123456, 0x4b3d2a),
                    Color(0x123456, 0x4b3d2a),
                    Color(0x123456, 0x4b3d2a),
                    Color(0x123456, 0x4b3d2a),
                ),
                ItemNameAnalytics("analytics"),
                Route.Push(FeatureInfo(FeatureKey("fakeFeature", 1)))
            )
        )
    }

}
