package com.greencopper.interfacekit.widgets.initializer

import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.titlesubtitlewidget.TitleSubtitleWidgetLayout
import com.greencopper.testmocks.interfacekit.MockWidgetParams
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class TitleSubtitleWidgetInitializerTest {

    private lateinit var initializer: TitleSubtitleWidgetInitializer

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        initializer = TitleSubtitleWidgetInitializer()
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
        assertThat(result).isInstanceOf(TitleSubtitleWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val data = TitleSubtitleWidgetParameters(
            "title",
            "subtitle",
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(TitleSubtitleWidgetParameters::class.java)
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
            initializer.resolveParams(TestWidgetParameters(1).encodeToJsonElement())
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
            TitleSubtitleWidgetParameters(
                "title",
                "subtitle",
            )
        )
    }

    @Serializable
    data class TestWidgetParameters(val title: Int) : KiboSerializable<TestWidgetParameters> {
        override fun getSerializer(): KSerializer<TestWidgetParameters> = serializer()
    }
}
