package com.greencopper.toolkit.extensions

import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import androidx.core.text.HtmlCompat
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

public fun String.decodeHtmlString(): CharSequence {

    val htmlDecoded = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)

    var start = 0
    var end = htmlDecoded.length

    while (start < end && Character.isWhitespace(htmlDecoded[start])) {
        start++
    }

    while (end > start && Character.isWhitespace(htmlDecoded[end - 1])) {
        end--
    }

    val ssb = SpannableStringBuilder(htmlDecoded.subSequence(start, end))
    ssb.getSpans(0, ssb.length, BulletSpan::class.java).forEach { span ->
        val spanStart = ssb.getSpanStart(span)
        val spanEnd = ssb.getSpanEnd(span)

        ssb.removeSpan(span)
        ssb.setSpan(BulletSpan(40), spanStart, spanEnd, 0)
    }

    return ssb
}

private const val REPLACE_TEMPLATE = "%@"

public fun String.formatTemplate(vararg strings: String): String {
    var result = this

    strings.forEach { string ->
        result = result.replaceFirst(REPLACE_TEMPLATE, string)
    }

    return result
}

public fun String?.toZonedDateTime(dateTimeFormatter: DateTimeFormatter? = null): ZonedDateTime? {
    this ?: return null
    return try {
        dateTimeFormatter?.let {
            ZonedDateTime.parse(this, it)
        } ?: ZonedDateTime.parse(this)
    } catch (throwable: Throwable) {
        App.log.e("Could not parse date", throwable = throwable)
        null
    }
}
