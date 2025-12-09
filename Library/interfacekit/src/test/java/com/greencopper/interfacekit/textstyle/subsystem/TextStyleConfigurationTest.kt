package com.greencopper.interfacekit.textstyle.subsystem

import com.greencopper.interfacekit.textstyle.subsystem.TextStyleConfiguration.*
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Test

internal class TextStyleConfigurationTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun testSerializable() = testKiboSerializable(TextStyleConfiguration(mapOf(
        "test" to Theme(DefaultSet(
            largeTitle = TextStyle(listOf(TextStyle.Font("largeTitle"))),
            title = Title(
                xl = TextStyle(listOf(TextStyle.Font("title_xl"))),
                l = TextStyle(listOf(TextStyle.Font("title_l"))),
                m = TextStyle(listOf(TextStyle.Font("title_m"))),
                s = TextStyle(listOf(TextStyle.Font("title_s"))),
                xs = TextStyle(listOf(TextStyle.Font("title_xs")))
            ),
            headline = Headline(
                l = TextStyle(listOf(TextStyle.Font("headline_l"))),
                m = TextStyle(listOf(TextStyle.Font("headline_m"))),
                s = TextStyle(listOf(TextStyle.Font("headline_s"))),
            ),
            body = Body(
                xl = TextStyle(listOf(TextStyle.Font("body_xl"))),
                l = TextStyle(listOf(TextStyle.Font("body_l"))),
                m = TextStyle(listOf(TextStyle.Font("body_m"))),
                s = TextStyle(listOf(TextStyle.Font("body_s"))),
                xs = TextStyle(listOf(TextStyle.Font("body_xs"))),
            ),
            caption = Caption(
                l = TextStyle(listOf(TextStyle.Font("caption_l"))),
                s = TextStyle(listOf(TextStyle.Font("caption_s"))),
            ),
            footnote = Footnote(
                m = TextStyle(listOf(TextStyle.Font("footnot_m"))),
                s = TextStyle(listOf(TextStyle.Font("footnot_s"))),
            ),
        ))
    )))

}
