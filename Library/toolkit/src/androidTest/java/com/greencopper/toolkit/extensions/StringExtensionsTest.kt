package com.greencopper.toolkit.extensions

import androidx.core.text.HtmlCompat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StringExtensionsTest {

    @Test
    fun html_shouldDecodeAndTrim() {
        val htmlString = "<br><br><p>This is <strong>a</strong> <u>test</u>"
        val withoutTrim = HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_COMPACT)
        val withTrim = htmlString.decodeHtmlString()

        assertThat(withoutTrim[0].isWhitespace()).isTrue
        assertThat(withoutTrim[withoutTrim.length - 1].isWhitespace()).isTrue
        assertThat(withTrim[0].isWhitespace()).isFalse
        assertThat(withTrim[withTrim.length - 1].isWhitespace()).isFalse
    }

    @Test
    fun givenNoTemplates_formatTemplate_unchangedResult() {
        val template = "test string"
        val result = template.formatTemplate("value1", "value2")
        assertThat(result).isEqualTo(template)
    }

    @Test
    fun givenMatchingTemplate_formatTemplate_replacesWithValue() {
        val template = "Template test: %@"
        val result = template.formatTemplate("value1")
        assertThat(result).isEqualTo("Template test: value1")
    }

    @Test
    fun givenTooManyValues_formatTemplate_replacesWithValue() {
        val template = "Template test: %@"
        val result = template.formatTemplate("value1", "value2")
        assertThat(result).isEqualTo("Template test: value1")
    }

    @Test
    fun givenNotEnoughValues_formateTemplate_unchangedResult() {
        val template = "Template test: %@"
        val result = template.formatTemplate()
        assertThat(result).isEqualTo(template)
    }

    @Test
    fun givenMultipleTemplates_formatTemplate_replacesMultipleValues() {
        val template = "%@ and %@"
        val result = template.formatTemplate("one", "two")
        assertThat(result).isEqualTo("one and two")
    }

    @Test
    fun givenMultipleTemplatesOneValue_formatTemplate_replacesSomeValues() {
        val template = "%@ and %@ and %@"
        val result = template.formatTemplate("one", "two")
        assertThat(result).isEqualTo("one and two and %@")
    }
}
