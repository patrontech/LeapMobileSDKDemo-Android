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
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.interfacekit.MockColorRepository
import com.greencopper.testmocks.interfacekit.MockLinkResolver
import com.greencopper.testmocks.interfacekit.MockTextStyleRepository
import com.greencopper.testmocks.interfacekit.MockWidgetParams
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ButtonWidgetV2InitializerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var initializer: ButtonWidgetV2Initializer

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        initializer = ButtonWidgetV2Initializer(MockLinkResolver())
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
        assertThat(result).isInstanceOf(ButtonWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val json = App.resolve<Json>()
        val deeplinkScheme = context.getString(R.string.deeplink_scheme)
        val paramString = "paramStringToReplace"
        val paramInt = "paramIntToReplace"
        val routeLink = "presentRoute"
        val route = Route.Present(
            FeatureInfo(
                FeatureKey("mockFeature", 1),
                json.parseToJsonElement("{ \"myParamString\": \"{#/$paramString}\", \"myParamInt\": \"{#/$paramInt}\"}")
            )
        )
        val onTap = "$deeplinkScheme://$routeLink?$paramString=pouet&$paramInt=42"
        val linkResolver = MockLinkResolver(
            mutableMapOf(
                onTap to route
            ),
            mutableMapOf()
        )
        initializer = ButtonWidgetV2Initializer(linkResolver)

        val data = ButtonWidgetV2Parameters(
            "iconName",
            "text",
            ButtonWidgetColors(
                Color(0x123456, 0x4b3d2a),
                Color(0x123456, 0x4b3d2a),
                Color(0x123456, 0x4b3d2a),
                Color(0x123456, 0x4b3d2a),
            ),
            ItemNameAnalytics("analytics"),
            onTap
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(ButtonWidgetParameters::class.java)
        result as ButtonWidgetParameters
        assertThat((result.onTap as Route.Present).feature.key).isEqualTo(route.feature.key)
    }

    @Test
    fun resolveParams_withBadRoute_shouldThrow() {
        //given
        val data = ButtonWidgetV2Parameters(
            "iconName",
            "text",
            ButtonWidgetColors(
                Color(0x123456, 0x4b3d2a),
                Color(0x123456, 0x4b3d2a),
                Color(0x123456, 0x4b3d2a),
                Color(0x123456, 0x4b3d2a),
            ),
            ItemNameAnalytics("analytics"),
            "deeplink://routeLink"
        )

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
    public fun resolveGenerator_shouldThrow() {
        assertThrows<NotImplementedError> {
            initializer.resolveGenerator(MockWidgetParams("name").encodeToJsonElement(), "screenName123", mockk())
        }
    }

    @Test
    fun serialize_deserialize_test() {
        testKiboSerializable(
            ButtonWidgetV2Parameters(
                "iconName",
                "text",
                ButtonWidgetColors(
                    Color(0x123456, 0x4b3d2a),
                    Color(0x123456, 0x4b3d2a),
                    Color(0x123456, 0x4b3d2a),
                    Color(0x123456, 0x4b3d2a),
                ),
                ItemNameAnalytics("analytics"),
                "onTap"
            )
        )
    }

}
