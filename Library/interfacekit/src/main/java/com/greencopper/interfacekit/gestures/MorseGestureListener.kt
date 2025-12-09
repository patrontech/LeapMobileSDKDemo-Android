package com.greencopper.interfacekit.gestures

import java.util.LinkedList
import java.util.Queue

public class MorseGestureListener {

    private var callback: (() -> Unit)? = null
    private var morsePattern: List<ClickType> = emptyList()
    private val gestures: Queue<ClickType> = LinkedList<ClickType>()

    public fun init(pattern: String, onGestureDetected: () -> Unit) {
        callback = onGestureDetected
        morsePattern = pattern.mapNotNull { char ->
            when (char) {
                '.' -> ClickType.Short
                '-' -> ClickType.Long
                else -> null
            }
        }
    }

    public fun onClick() {
        addGesture(ClickType.Short)
    }

    public fun onLongClick() {
        addGesture(ClickType.Long)
    }

    private fun addGesture(clickType: ClickType) {
        gestures.add(clickType)
        if (gestures.size > morsePattern.size) gestures.remove()
        if (gestures == morsePattern) {
            callback?.invoke()
            gestures.clear()
        }
    }

    private enum class ClickType { Short, Long }
}
