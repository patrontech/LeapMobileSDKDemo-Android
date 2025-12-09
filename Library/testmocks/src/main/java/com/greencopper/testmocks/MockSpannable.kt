package com.greencopper.testmocks

import android.text.Spannable

public class MockSpannable(public val string: String) : Spannable {
    override fun get(index: Int): Char = string[index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = string.subSequence(startIndex, endIndex)

    override fun <T : Any?> getSpans(start: Int, end: Int, type: Class<T>?): Array<T> {
        TODO("Not yet implemented")
    }

    override fun getSpanStart(tag: Any?): Int {
        TODO("Not yet implemented")
    }

    override fun getSpanEnd(tag: Any?): Int {
        TODO("Not yet implemented")
    }

    override fun getSpanFlags(tag: Any?): Int {
        TODO("Not yet implemented")
    }

    override fun nextSpanTransition(start: Int, limit: Int, type: Class<*>?): Int {
        TODO("Not yet implemented")
    }

    override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {
        TODO("Not yet implemented")
    }

    override fun removeSpan(what: Any?) {
        TODO("Not yet implemented")
    }

    override val length: Int
        get() = string.length
}
