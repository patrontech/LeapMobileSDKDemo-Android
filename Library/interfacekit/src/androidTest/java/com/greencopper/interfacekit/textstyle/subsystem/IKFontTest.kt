package com.greencopper.interfacekit.textstyle.subsystem

import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.resources.TextAppearance
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleConfiguration.TextStyle
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleConfiguration.TextStyle.Font
import com.greencopper.interfacekit.ui.spToPx
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.interfacekit.MockTextStyleRepository
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class IKFontTest {

    private val mockTextStyleRepository = MockTextStyleRepository()
    private val context = InstrumentationRegistry.getInstrumentation().context

    init {
        Toolkit.setupTest()
        bindSingleton<TextStyleRepository>(mockTextStyleRepository)
    }

    @Test
    fun testEnumValues() {
        with(IKFont.TextStyle.largeTitle) {
            assertThat(textSize).isEqualTo(34f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(34f.spToPx())
            assertThat(themeDefault.largeTitle).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.titleXL) {
            assertThat(textSize).isEqualTo(28f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(28f.spToPx())
            assertThat(themeDefault.title.xl).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.titleL) {
            assertThat(textSize).isEqualTo(24f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(24f.spToPx())
            assertThat(themeDefault.title.l).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.titleM) {
            assertThat(textSize).isEqualTo(20f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(20f.spToPx())
            assertThat(themeDefault.title.m).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.titleS) {
            assertThat(textSize).isEqualTo(18f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(18f.spToPx())
            assertThat(themeDefault.title.s).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.titleXS) {
            assertThat(textSize).isEqualTo(12f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(12f.spToPx())
            assertThat(themeDefault.title.xs).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.headlineL) {
            assertThat(textSize).isEqualTo(16f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(16f.spToPx())
            assertThat(themeDefault.headline.l).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.headlineM) {
            assertThat(textSize).isEqualTo(15f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(15f.spToPx())
            assertThat(themeDefault.headline.m).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.headlineS) {
            assertThat(textSize).isEqualTo(12f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(12f.spToPx())
            assertThat(themeDefault.headline.s).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.bodyXL) {
            assertThat(textSize).isEqualTo(18f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(18f.spToPx())
            assertThat(themeDefault.body.xl).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.bodyL) {
            assertThat(textSize).isEqualTo(16f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(16f.spToPx())
            assertThat(themeDefault.body.l).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.bodyM) {
            assertThat(textSize).isEqualTo(15f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(15f.spToPx())
            assertThat(themeDefault.body.m).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.bodyS) {
            assertThat(textSize).isEqualTo(14f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(14f.spToPx())
            assertThat(themeDefault.body.s).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.bodyXS) {
            assertThat(textSize).isEqualTo(12f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(12f.spToPx())
            assertThat(themeDefault.body.xs).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.captionL) {
            assertThat(textSize).isEqualTo(14f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(14f.spToPx())
            assertThat(themeDefault.caption.l).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.captionS) {
            assertThat(textSize).isEqualTo(14f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(14f.spToPx())
            assertThat(themeDefault.caption.s).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.footnoteM) {
            assertThat(textSize).isEqualTo(10f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(10f.spToPx())
            assertThat(themeDefault.footnote.m).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }
        with(IKFont.TextStyle.footnoteS) {
            assertThat(textSize).isEqualTo(10f)
            assertThat(TextAppearance(context, textSizeAsTextAppearance).textSize)
                .isEqualTo(10f.spToPx())
            assertThat(themeDefault.footnote.s).isEqualTo(getDefaultFromTheme(themeDefault))
            assertThat(fallbackFont).isNotNull
        }

    }

    val themeDefault: TextStyleConfiguration.DefaultSet = TextStyleConfiguration.DefaultSet(
        largeTitle = TextStyle(fonts = listOf(Font("largeTitle"))),
        title = TextStyleConfiguration.Title(
            xl = TextStyle(fonts = listOf(Font("title_xl_1"))),
            l = TextStyle(fonts = listOf(Font("title_l_1"))),
            m = TextStyle(fonts = listOf(Font("title_m_1"))),
            s = TextStyle(fonts = listOf(Font("title_s_1"))),
            xs = TextStyle(fonts = listOf(Font("title_xs_1")))
        ),
        headline = TextStyleConfiguration.Headline(
            l = TextStyle(fonts = listOf(Font("headline_l_1"))),
            m = TextStyle(fonts = listOf(Font("headline_m_1"))),
            s = TextStyle(fonts = listOf(Font("headline_s_1")))
        ),
        body = TextStyleConfiguration.Body(
            xl = TextStyle(fonts = listOf(Font("body_xl_1"))),
            l = TextStyle(fonts = listOf(Font("body_l_1"))),
            m = TextStyle(fonts = listOf(Font("body_m_1"))),
            s = TextStyle(fonts = listOf(Font("body_s_1"))),
            xs = TextStyle(fonts = listOf(Font("body_xs_1")))
        ),
        caption = TextStyleConfiguration.Caption(
            l = TextStyle(fonts = listOf(Font("caption_l_1"))),
            s = TextStyle(fonts = listOf(Font("caption_s_1")))
        ),
        footnote = TextStyleConfiguration.Footnote(
            m = TextStyle(fonts = listOf(Font("footnote_m_1"))),
            s = TextStyle(fonts = listOf(Font("footnote_s_1")))
        )

    )

}

