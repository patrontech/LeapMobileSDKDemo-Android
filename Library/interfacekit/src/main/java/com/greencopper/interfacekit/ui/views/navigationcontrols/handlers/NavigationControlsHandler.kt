package com.greencopper.interfacekit.ui.views.navigationcontrols.handlers

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.getParentPresentedFragment
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler.Companion.NAVIGATION_BUTTONS_FLAGS_KEY
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler.Companion.SHOW_BACK_BUTTON_FLAG
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler.Companion.SHOW_CLOSE_BUTTON_FLAG

public interface NavigationControlsHandler {

    public val resources: Resources

    public fun setup()

    public class DefaultBackPressedListener(private val activity: FragmentActivity?) : View.OnClickListener {
        override fun onClick(v: View?) {
            defaultBackPressedBehavior(activity)
        }
    }

    public class DefaultCloseClickListener(private val layout: Layout) : View.OnClickListener {
        override fun onClick(v: View?) {
            defaultClosePressedBehavior(layout)
        }
    }

    public companion object {
        public const val SHOW_BACK_BUTTON_FLAG: Int = 0X00000001
        public const val SHOW_CLOSE_BUTTON_FLAG: Int = 0X00000010
        public const val NAVIGATION_BUTTONS_FLAGS_KEY: String = "navigationButtonsFlags"
    }
}

public fun Fragment.getNavigationButtonsFlags(): Int {
    return arguments?.getInt(NAVIGATION_BUTTONS_FLAGS_KEY, 0) ?: 0
}

public fun Fragment.addNavigationButtonsFlags(flags: Int) {
    val argBundle = arguments ?: Bundle().also {
        arguments = it
    }
    val currentFlags = getNavigationButtonsFlags()
    argBundle.putInt(NAVIGATION_BUTTONS_FLAGS_KEY, currentFlags or flags)
}

public fun Fragment.shouldShowBackButton(): Boolean {
    return getNavigationButtonsFlags() and SHOW_BACK_BUTTON_FLAG == SHOW_BACK_BUTTON_FLAG
}

public fun Fragment.shouldShowCloseButton(): Boolean {
    return getNavigationButtonsFlags() and SHOW_CLOSE_BUTTON_FLAG == SHOW_CLOSE_BUTTON_FLAG
}

public fun Layout.closePresentedLayout() {
    getParentPresentedFragment()?.parentFragment?.childFragmentManager?.popBackStack()
        ?: activity?.onBackPressedDispatcher?.onBackPressed()
}

public fun defaultBackPressedBehavior(activity: FragmentActivity?) {
    activity?.onBackPressedDispatcher?.onBackPressed()
}

public fun defaultClosePressedBehavior(layout: Layout) {
    layout.closePresentedLayout()
}
