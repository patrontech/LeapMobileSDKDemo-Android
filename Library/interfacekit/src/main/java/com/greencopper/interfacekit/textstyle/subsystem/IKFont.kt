package com.greencopper.interfacekit.textstyle.subsystem

import android.graphics.Typeface
import androidx.annotation.StyleRes
import com.greencopper.interfacekit.R

public data class IKFont(
    internal val textStyle: TextStyle,
    internal val overrides: List<TextStyleConfiguration.TextStyle> = emptyList(),
    public val typeface: Typeface = textStyle.fallbackFont,
) {
    public val fontSize: Float get() = textStyle.textSize

    @get:StyleRes
    public val fontSizeAsTextAppearance: Int get() = textStyle.textSizeAsTextAppearance

    @Suppress("EnumEntryName")
    public enum class TextStyle {
        largeTitle,
        titleXL,
        titleL,
        titleM,
        titleS,
        titleXS,
        headlineL,
        headlineM,
        headlineS,
        bodyXL,
        bodyL,
        bodyM,
        bodyS,
        bodyXS,
        captionL,
        captionS,
        footnoteM,
        footnoteS;

        public val textSize: Float by lazy {
            when (this) {
                largeTitle -> 34f
                titleXL -> 28f
                titleL -> 24f
                titleM -> 20f
                titleS -> 18f
                titleXS -> 12f
                headlineL -> 16f
                headlineM -> 15f
                headlineS -> 12f
                bodyXL -> 18f
                bodyL -> 16f
                bodyM -> 15f
                bodyS -> 14f
                bodyXS -> 12f
                captionL -> 14f
                captionS -> 14f
                footnoteM -> 10f
                footnoteS -> 10f
            }
        }

        @get:StyleRes
        public val textSizeAsTextAppearance: Int by lazy {
            when (this) {
                largeTitle -> R.style.TextAppearance_TextStyle_LargeTitle
                titleXL -> R.style.TextAppearance_TextStyle_TitleXL
                titleL -> R.style.TextAppearance_TextStyle_TitleL
                titleM -> R.style.TextAppearance_TextStyle_TitleM
                titleS -> R.style.TextAppearance_TextStyle_TitleS
                titleXS -> R.style.TextAppearance_TextStyle_TitleXS
                headlineL -> R.style.TextAppearance_TextStyle_HeadlineL
                headlineM -> R.style.TextAppearance_TextStyle_HeadlineM
                headlineS -> R.style.TextAppearance_TextStyle_HeadlineS
                bodyXL -> R.style.TextAppearance_TextStyle_BodyXL
                bodyL -> R.style.TextAppearance_TextStyle_BodyL
                bodyM -> R.style.TextAppearance_TextStyle_BodyM
                bodyS -> R.style.TextAppearance_TextStyle_BodyS
                bodyXS -> R.style.TextAppearance_TextStyle_BodyXS
                captionL -> R.style.TextAppearance_TextStyle_CaptionL
                captionS -> R.style.TextAppearance_TextStyle_CaptionS
                footnoteM -> R.style.TextAppearance_TextStyle_FootnoteM
                footnoteS -> R.style.TextAppearance_TextStyle_FootnoteS
            }
        }

        public fun getDefaultFromTheme(defaultTheme: TextStyleConfiguration.DefaultSet): TextStyleConfiguration.TextStyle =
            when (this) {
                largeTitle -> defaultTheme.largeTitle
                titleXL -> defaultTheme.title.xl
                titleL -> defaultTheme.title.l
                titleM -> defaultTheme.title.m
                titleS -> defaultTheme.title.s
                titleXS -> defaultTheme.title.xs
                headlineL -> defaultTheme.headline.l
                headlineM -> defaultTheme.headline.m
                headlineS -> defaultTheme.headline.s
                bodyXL -> defaultTheme.body.xl
                bodyL -> defaultTheme.body.l
                bodyM -> defaultTheme.body.m
                bodyS -> defaultTheme.body.s
                bodyXS -> defaultTheme.body.xs
                captionL -> defaultTheme.caption.l
                captionS -> defaultTheme.caption.s
                footnoteM -> defaultTheme.footnote.m
                footnoteS -> defaultTheme.footnote.s
            }

        public val fallbackFont: Typeface by lazy {
            when (this) {
                largeTitle -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.bold).build()
                titleXL -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.bold).build()
                titleL -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.bold).build()
                titleM -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.bold).build()
                titleS -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.bold).build()
                titleXS -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.bold).build()
                headlineL -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.bold).build()
                headlineM -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.semiBold).build()
                headlineS -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.semiBold).build()
                bodyXL -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.regular).build()
                bodyL -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.regular).build()
                bodyM -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.medium).build()
                bodyS -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.regular).build()
                bodyXS -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.regular).build()
                captionL -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.bold).build()
                captionS -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.semiBold).build()
                footnoteM -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.semiBold).build()
                footnoteS -> Typeface.Builder("").setFallback("roboto").setWeight(FontWeight.regular).build()
            }
        }

    }

}
