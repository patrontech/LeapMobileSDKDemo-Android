package com.greencopper.interfacekit.textstyle.subsystem

import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.greencopper.interfacekit.ui.spToPx

public fun TextView.setFont(font: IKFont) {
    typeface = font.typeface
    textSize = font.fontSize
}

public fun Button.setFont(font: IKFont) {
    typeface = font.typeface
    textSize = font.fontSize
}

public fun MenuItem.setFont(font: IKFont) {
    setFont(font.typeface, font.fontSize)
}

public fun MenuItem.setFont(typeface: Typeface, fontSize: Float) {
    title = getSpannableString(title, typeface, fontSize)
}

/**
 * This function sets the hint and text font. To set the error message's font, use [TextInputLayout.setErrorFont]
 */
public fun TextInputLayout.setFont(textInputEditText: TextInputEditText, font: IKFont) {
    typeface = font.typeface
    textInputEditText.typeface = font.typeface
    textInputEditText.textSize = font.fontSize
}

public fun TextInputLayout.setErrorFont(text: String, font: IKFont) {
    error = getSpannableString(text, font.typeface, font.fontSize)
}

private class IKFontSpan(private val typeface: Typeface, private val fontSize: Float) : MetricAffectingSpan() {
    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, typeface, fontSize)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, typeface, fontSize)
    }

    private companion object {
        private fun applyCustomTypeFace(paint: Paint, typeface: Typeface, fontSize: Float) {
            paint.flags = paint.flags or Paint.SUBPIXEL_TEXT_FLAG
            paint.typeface = typeface
            paint.textSize = fontSize.spToPx()
        }
    }
}

private fun getSpannableString(charSequence: CharSequence?, typeface: Typeface, fontSize: Float): SpannableStringBuilder =
    SpannableStringBuilder().append(
        charSequence,
        IKFontSpan(typeface, fontSize),
        Spanned.SPAN_INCLUSIVE_INCLUSIVE,
    )

