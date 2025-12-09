package com.greencopper.interfacekit.common

import android.os.SystemClock
import android.view.View

public class SafeClickListener(
    private var securedInterval: Int = 1000,
    private val onSafeClick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < securedInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeClick(v)
    }
}

public fun View.setOnSafeClickListener(securedInterval: Int = 1000, onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener(securedInterval) {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}
