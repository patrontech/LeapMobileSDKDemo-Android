package com.greencopper.interfacekit.textstyle.subsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.unit.sp
import com.greencopper.interfacekit.ui.compose.mockTextStyle
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockTextStyleRepository
import com.greencopper.toolkit.Toolkit
import de.mannodermaus.junit5.compose.createComposeExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.RegisterExtension

internal class UITextStyleTest {

    private val textStyleRepository = MockTextStyleRepository()

    init {
        Toolkit.setupTest()
        bindSingleton<TextStyleRepository>(textStyleRepository)
    }

    @Test
    fun whenGettingIKFont_shouldBuildCorrectly() {
        InterfaceKitTextStyleTest.sample.largeTitle

        val iKFontCalls = textStyleRepository.iKFontCalls
        assertThat(iKFontCalls).hasSize(2)
        iKFontCalls[0].levels shouldBe listOf("interfaceKit", "sample", "text")
        iKFontCalls[1].levels shouldBe listOf("interfaceKit", "sample", "largeTitle")
        iKFontCalls[1].textStyle shouldBe IKFont.TextStyle.largeTitle
        assertThat(iKFontCalls[1].fallbacks).hasSize(1)
        iKFontCalls[1].fallbacks.first().typeface shouldBe InterfaceKitTextStyleTest.sample.text.typeface
    }

    @Test
    fun whenGettingTopBar_shouldBuildCorrectly() {
        InterfaceKitTextStyleTest.sample.topBar.title.large
        InterfaceKitTextStyleTest.sample.topBar.title.normal

        val iKFontCalls = textStyleRepository.iKFontCalls
        assertThat(iKFontCalls).hasSize(2)

        iKFontCalls[0].levels shouldBe listOf("interfaceKit", "sample", "topBar", "title", "large")
        iKFontCalls[0].textStyle shouldBe IKFont.TextStyle.largeTitle
        assertThat(iKFontCalls[0].fallbacks).hasSize(0)

        iKFontCalls[1].levels shouldBe listOf("interfaceKit", "sample", "topBar", "title", "normal")
        iKFontCalls[1].textStyle shouldBe IKFont.TextStyle.titleS
        assertThat(iKFontCalls[1].fallbacks).hasSize(0)
    }

    @Test
    fun whenGettingIKFont_withUnknownLeaf_shouldBuildCorrectly() {
        InterfaceKitTextStyleTest.sample.notExistingLeaf

        val iKFontCalls = textStyleRepository.iKFontCalls
        assertThat(iKFontCalls).hasSize(1)
        iKFontCalls[0].levels shouldBe listOf("interfaceKit", "sample", "test")
        iKFontCalls[0].textStyle shouldBe IKFont.TextStyle.captionL
        assertThat(iKFontCalls[0].fallbacks).hasSize(0)
    }

}

@OptIn(ExperimentalTestApi::class)
@Nested
internal class UITextStyleComposeTests {
    @JvmField
    @RegisterExtension
    @ExperimentalTestApi
    val extension = createComposeExtension()

    @Test
    @Disabled("UI test failing")
    fun whenGettingIKFont_shouldBuildCorrectly() {
        extension.use {
            setContent {
                CompositionLocalProvider(
                    mockTextStyle(
                        mapOf(
                            listOf("interfaceKit", "sample", "subtitle") to IKFont(IKFont.TextStyle.bodyS)
                        )
                    )
                ) {
                    val textStyle = InterfaceKitTextStyleTest.sample.subtitle

                    textStyle.fontSize shouldBe IKFont.TextStyle.bodyS.textSize.sp
                }
            }
        }
    }
}

internal object InterfaceKitTextStyleTest : UITextStyle() {
    override val level: String = "interfaceKit"

    val sample = Sample(this)

    class Sample(parent: InterfaceKitTextStyleTest) : ScreenTextStyle(parent) {
        override val level: String = "sample"

        val text: IKFont get() = toIKFont("text")
        val largeTitle: IKFont get() = toIKFont("largeTitle", IKFont.TextStyle.largeTitle, text)

        val notExistingLeaf: IKFont get() = toIKFont("test", IKFont.TextStyle.captionL)

        val subtitle @Composable get() = composeIKFont("subtitle", IKFont.TextStyle.headlineL)
    }
}
