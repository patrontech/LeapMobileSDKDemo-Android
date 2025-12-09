package com.greencopper.interfacekit.common

import android.view.ViewTreeObserver
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

internal class KeyboardHelper {
    private var isKeyboardShown = false

    private var keyboardListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    fun setKeyboardAppearanceListener(window: Window, listener: KeyboardListener) {
        keyboardListener = ViewTreeObserver.OnGlobalLayoutListener {
            val showingKeyboard = ViewCompat.getRootWindowInsets(window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime())
                ?: false

            // addOnGlobalLayoutListener can be called multiple times
            // so it's better to check if status of keyboard was changed before doing something
            if (isKeyboardShown != showingKeyboard) {
                isKeyboardShown = showingKeyboard

                if (isKeyboardShown) {
                    listener.onKeyboardShowing()
                } else {
                    listener.onKeyboardHiding()
                }
            }
        }
        window.decorView.viewTreeObserver.addOnGlobalLayoutListener(keyboardListener)
    }

    fun removeListener(window: Window){
        keyboardListener?.let {
            window.decorView.viewTreeObserver.removeOnGlobalLayoutListener(it)
        }
    }

    interface KeyboardListener {
        fun onKeyboardShowing()
        fun onKeyboardHiding()
    }
}