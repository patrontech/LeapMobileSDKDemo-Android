package com.greencopper.interfacekit.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View

public class OnTouchClickListener(
    context: Context,
    private val onTouchInternal: (view: View, motionEvent: MotionEvent) -> Boolean,
    private val onClick: (motionEvent: MotionEvent) -> Unit,
    private val onDoubleClick: (motionEvent: MotionEvent) -> Unit = {},
    private val onLongClick: (motionEvent: MotionEvent) -> Unit = {},
    private val safeClickInterval: Int = 1000,
) : View.OnTouchListener {

    private val gestureDetector = GestureDetector(context, GestureListener())
    private var lastTimeClicked: Long = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return onTouchInternal(view, motionEvent) || gestureDetector.onTouchEvent(motionEvent)
    }

    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
            if (SystemClock.elapsedRealtime() - lastTimeClicked > safeClickInterval) {
                onClick(motionEvent)
                lastTimeClicked = SystemClock.elapsedRealtime()
            }
            return super.onSingleTapUp(motionEvent)
        }

        override fun onDoubleTap(motionEvent: MotionEvent): Boolean {
            onDoubleClick(motionEvent)
            return super.onDoubleTap(motionEvent)
        }

        override fun onLongPress(motionEvent: MotionEvent) {
            onLongClick(motionEvent)
            super.onLongPress(motionEvent)
        }
    }
}
