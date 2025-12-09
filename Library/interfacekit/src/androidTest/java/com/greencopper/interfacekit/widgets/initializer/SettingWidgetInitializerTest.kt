package com.greencopper.interfacekit.widgets.initializer

import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.Color
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.setting.SettingWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

public class SettingWidgetInitializerTest {
    private lateinit var initializer: SettingWidgetInitializer

    @BeforeEach
    public fun beforeEach() {
        Toolkit.setupTest()

        initializer = SettingWidgetInitializer()
    }

    @Test
    public fun resolveLayout_shouldGetLayout() {
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
        assertThat(result).isInstanceOf(SettingWidgetLayout::class.java)
    }

    @Test
    public fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val data = SettingWidgetParameters(
            SettingWidgetParameters.Label("Title", Color(1, 1)),
            SettingWidgetParameters.Label("Subtitle", Color(1, 1)),
            SettingWidgetParameters.OnTap("routeLink", ItemNameAnalytics("itemName"))
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(SettingWidgetParameters::class.java)
        assertThat(result).isEqualTo(data)
    }

    @Test
    public fun resolveParams_withoutParams_shouldThrow() {
        assertThrows<WidgetException.NoParametersProvided> {
            initializer.resolveParams(null)
        }
    }

    @Test
    public fun resolveParams_withBadParams_shouldThrow() {
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
    public fun serialize_deserialize_test() {
        testKiboSerializable(
            SettingWidgetParameters(
                SettingWidgetParameters.Label("Title", Color(1, 1)),
                SettingWidgetParameters.Label("Subtitle", Color(1, 1)),
                SettingWidgetParameters.OnTap("routeLink", ItemNameAnalytics("itemName"))
            )
        )
    }
}
