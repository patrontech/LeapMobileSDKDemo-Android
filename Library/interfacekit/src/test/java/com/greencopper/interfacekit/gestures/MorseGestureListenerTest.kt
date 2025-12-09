package com.greencopper.interfacekit.gestures

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MorseGestureListenerTest {

    var matched = false
    private val gestureListener = MorseGestureListener().apply {
        init("-.-") { matched = true }
    }

    @Test
    fun patternNotMatched_callbackNeverTriggers() {
        gestureListener.onClick()
        gestureListener.onClick()
        gestureListener.onClick()
        assertThat(matched).isFalse

        gestureListener.onLongClick()
        gestureListener.onLongClick()
        gestureListener.onLongClick()
        assertThat(matched).isFalse

        gestureListener.onLongClick()
        gestureListener.onClick()
        gestureListener.onClick()
        gestureListener.onLongClick()
        assertThat(matched).isFalse
    }

    @Test
    fun callbackTriggers_whenPatternMatched() {
        gestureListener.onLongClick()
        gestureListener.onClick()
        gestureListener.onLongClick()
        assertThat(matched).isTrue
    }

    @Test
    fun callbackTriggers_whenPatternEventuallyMatched() {
        gestureListener.onLongClick()
        gestureListener.onLongClick()
        gestureListener.onClick()
        gestureListener.onClick()
        gestureListener.onLongClick()
        gestureListener.onLongClick()
        gestureListener.onLongClick()
        gestureListener.onClick()
        gestureListener.onLongClick()
        assertThat(matched).isTrue
    }

    @Test
    fun invalidCharacters_notAddedToPattern() {
        gestureListener.init("12345.-.") { matched = true }

        gestureListener.onClick()
        gestureListener.onLongClick()
        gestureListener.onClick()
        assertThat(matched).isTrue
    }
}
