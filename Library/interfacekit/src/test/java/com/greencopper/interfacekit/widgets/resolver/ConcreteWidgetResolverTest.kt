package com.greencopper.interfacekit.widgets.resolver

import android.content.Context
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetInfo
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.initializer.WidgetInitializer
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.MockWidgetGenerator
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.interfacekit.MockWidgetInitializer
import com.greencopper.testmocks.interfacekit.MockWidgetParams
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ConcreteWidgetResolverTest {

    private lateinit var widgetResolver: ConcreteWidgetResolver
    private val context = mockk<Context>(relaxed = true)
    private val mockWidgetParams = MockWidgetParams("testName")
    private val widgetInitializer = MockWidgetInitializer(mockParams = { mockWidgetParams })
    private lateinit var widgetInfo: WidgetInfo

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        widgetInfo = WidgetInfo(
            WidgetKey("MockWidget", 1),
            mockWidgetParams.encodeToJsonElement()
        )
        bindProvider<WidgetInitializer>(widgetInitializer, widgetInfo.key)

        widgetResolver = ConcreteWidgetResolver(App)
    }

    @Test
    fun resolveLayout_withUnknownKey_shouldThrow() {
        assertThrows<WidgetNotFoundException> {
            widgetResolver.resolveLayout(WidgetKey("wrongKey", 1), context)
        }
    }

    @Test
    fun resolveLayout_withKnownKey_shouldResolveLayout() {
        //when
        val result = widgetResolver.resolveLayout(widgetInfo.key, context)

        //then
        assertThat(widgetInitializer.layoutsResolved).isEqualTo(1)
        assertThat(result).isInstanceOf(WidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withUnknownKey_shouldThrow() {
        assertThrows<WidgetNotFoundException> {
            widgetResolver.resolveParams(
                WidgetInfo(
                    WidgetKey("wrongKey", 1),
                    mockWidgetParams.encodeToJsonElement()
                )
            )
        }
    }

    @Test
    fun resolveParams_withKnownKey_shouldReturnParams() {
        //when
        val result = widgetResolver.resolveParams(widgetInfo)

        //then
        assertThat(result).isEqualTo(mockWidgetParams)
    }

    @Test
    fun resolveWidgets_returnListOfWidgets() {
        //when
        val result = widgetResolver.resolveWidgets()

        //then
        assertThat(result).isEqualTo(listOf(widgetInitializer.key))
    }

    @Test
    fun resolveGenerator_returnWidgetGenerator() {
        val expectedScreenName = "screenName123"
        val expectedOrigin = mockk<Layout>()
        val mockGenerator = MockWidgetGenerator()
        widgetInitializer.mockGenerator = { jsonWidgetParams, screenName, origin ->
            jsonWidgetParams shouldBe mockWidgetParams.encodeToJsonElement()
            screenName shouldBe expectedScreenName
            origin shouldBe expectedOrigin
            mockGenerator
        }

        //when
        val result = widgetResolver.resolveGenerator(widgetInfo, expectedScreenName, expectedOrigin)

        //then
        result shouldBe mockGenerator
    }

    @Test
    fun resolveUnknownGenerator_shouldThrow() {
        val expectedScreenName = "screenName123"
        val expectedOrigin = mockk<Layout>()
        val mockGenerator = MockWidgetGenerator()
        widgetInitializer.mockGenerator = { jsonWidgetParams, screenName, origin ->
            jsonWidgetParams shouldBe mockWidgetParams.encodeToJsonElement()
            screenName shouldBe expectedScreenName
            origin shouldBe expectedOrigin
            mockGenerator
        }

        assertThrows<WidgetNotFoundException> {
            widgetResolver.resolveGenerator(
                WidgetInfo(
                    WidgetKey("wrongKey", 1),
                    mockWidgetParams.encodeToJsonElement()
                ), expectedScreenName, expectedOrigin
            )
        }
    }
}
