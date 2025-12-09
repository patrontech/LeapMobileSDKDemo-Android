package com.greencopper.interfacekit.widgets.initializer

import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.counter.*
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.titlecounterwidget.TitleCounterWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class TitleCounterWidgetInitializerTest {
    private lateinit var initializer: TitleCounterWidgetInitializer
    private lateinit var counterResolver: CounterResolver

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        counterResolver = DICounterResolver(App, MockLogging())
        initializer = TitleCounterWidgetInitializer(counterResolver)
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
        assertThat(result).isInstanceOf(TitleCounterWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val params = MockCounter.MockCounterParams().encodeToJsonElement()
        val mockCounter = MockCounter(Counter.Key("Counter", 1), params)
        val data = TitleCounterWidgetParameters(
            "iconName",
            "text",
            TitleCounterWidgetParameters.OnTap(
                "routeLink",
                ItemNameAnalytics("itemName")
            ),
            TitleCounterWidgetParameters.Counter(
                mockCounter.key,
                params
            )
        )

        //when
        bindCounter(mockCounter.key) { mockCounter }
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(TitleCounterWidgetParameters::class.java)
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
            TitleCounterWidgetParameters(
                "iconName",
                "text",
                TitleCounterWidgetParameters.OnTap(
                    "routeLink",
                    ItemNameAnalytics("itemName")
                ),
                TitleCounterWidgetParameters.Counter(
                    Counter.Key("Counter", 1),
                    MockCounter.MockCounterParams().encodeToJsonElement()
                )
            )
        )
    }
}
