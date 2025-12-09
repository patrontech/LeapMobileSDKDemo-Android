package com.greencopper.interfacekit.ui

import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.ActionBar

public fun ActionBar.setBackgroundColor(color: Int) {
    setBackgroundDrawable(ColorDrawable(color))
}

public fun ActionBar.setTitleColor(color: Int) {
    val text = SpannableString(title ?: "")
    text.setSpan(ForegroundColorSpan(color), 0, text.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    title = text
}