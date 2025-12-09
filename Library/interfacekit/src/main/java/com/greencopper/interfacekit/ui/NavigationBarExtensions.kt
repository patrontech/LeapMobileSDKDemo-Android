package com.greencopper.interfacekit.ui

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.versionprovider.BuildConfigProvider

public fun setNavigationBarColor(
    activity: Activity?,
    color: Int = InterfaceKitColor.navigationBar,
) {
    setNavigationBarColor(activity?.window, color)
}

public fun setNavigationBarColor(
    dialog: Dialog?,
    color: Int = InterfaceKitColor.navigationBar,
) {
    setNavigationBarColor(dialog?.window, color)
}

private fun setNavigationBarColor(window: Window?, color: Int) {
    window?.let {
        val isDark = ColorUtils.calculateLuminance(color) < 0.5
        if (App.resolve<BuildConfigProvider>().sdkInt > Build.VERSION_CODES.Q) {
            val navbar = WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            val appearance = if (isDark) 0 else navbar
            it.insetsController?.setSystemBarsAppearance(appearance, navbar)
        } else {
            val view = it.decorView
            view.systemUiVisibility = if (isDark) {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            } else {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
        it.navigationBarColor = color
    }
}

public var Fragment.shouldColorNavigationBar: Boolean
    get() = arguments?.getBoolean(SHOULD_COLOR_NAVIGATION_BAR_KEY, true) ?: true
    set(value) {
        val argBundle = arguments ?: Bundle().also {
            arguments = it
        }
        argBundle.putBoolean(SHOULD_COLOR_NAVIGATION_BAR_KEY, value)
    }

public const val SHOULD_COLOR_NAVIGATION_BAR_KEY: String = "shouldColorNavigationBar"
