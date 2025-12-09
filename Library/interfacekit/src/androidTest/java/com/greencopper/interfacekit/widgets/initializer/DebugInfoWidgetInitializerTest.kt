package com.greencopper.interfacekit.widgets.initializer

import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.debuginfowidget.DebugInfoWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

public class DebugInfoWidgetInitializerTest {
    private lateinit var initializer: DebugInfoWidgetInitializer

    @BeforeEach
    public fun beforeEach() {
        Toolkit.setupTest()

        initializer = DebugInfoWidgetInitializer()
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
        assertThat(result).isInstanceOf(DebugInfoWidgetLayout::class.java)
    }

    @Test
    public fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val data = DebugInfoWidgetParameters(
            DebugInfoWidgetParameters.MorseRoute("routeLink", "sequence"),
            listOf(
                DebugInfoWidgetParameters.Info(
                    "localStorage",
                    "version",
                    "localStorageKey"
                ),
                DebugInfoWidgetParameters.Info(
                    "localStorage",
                    "ota",
                    "localStorageKey"
                )
            )
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(DebugInfoWidgetParameters::class.java)
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
            DebugInfoWidgetParameters(
                DebugInfoWidgetParameters.MorseRoute("routeLink", "sequence"),
                listOf(
                    DebugInfoWidgetParameters.Info(
                        "localStorage",
                        "version",
                        "localStorageKey"
                    ),
                    DebugInfoWidgetParameters.Info(
                        "localStorage",
                        "ota",
                        "localStorageKey"
                    )
                )
            )
        )
    }
}
