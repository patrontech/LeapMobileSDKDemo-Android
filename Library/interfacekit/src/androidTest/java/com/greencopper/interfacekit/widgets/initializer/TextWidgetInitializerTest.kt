package com.greencopper.interfacekit.widgets.initializer

import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.textwidget.TextWidgetLayout
import com.greencopper.testmocks.interfacekit.MockWidgetParams
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class TextWidgetInitializerTest {

    private lateinit var initializer: TextWidgetInitializer

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        initializer = TextWidgetInitializer()
    }

    @Test
    fun resolveLayout_shouldGetLayout() {
        //given
        val context = ContextThemeWrapper(
            InstrumentationRegistry.getInstrumentation().targetContext,
            R.style.Base_Theme_MaterialComponents
        )

        //when
        val result = initializer.resolveLayout(context)

        //then
        assertThat(result).isInstanceOf(TextWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val data = TextWidgetParameters(
            "imageName",
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(TextWidgetParameters::class.java)
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
            TextWidgetParameters(
                "imageName",
            )
        )
    }

}
