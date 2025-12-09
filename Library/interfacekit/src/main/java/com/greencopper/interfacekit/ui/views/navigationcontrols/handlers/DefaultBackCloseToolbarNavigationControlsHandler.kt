package com.greencopper.interfacekit.ui.views.navigationcontrols.handlers

import android.content.res.Resources
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.greencopper.interfacekit.color.TopBarColor
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.subsystem.TopBarTextStyle
import com.greencopper.interfacekit.ui.views.navigationcontrols.KibaToolbar

public class DefaultBackCloseToolbarNavigationControlsHandler(
    private val fragment: Layout,
    private val toolbar: KibaToolbar,
    private val topBarColor: TopBarColor,
    private val topBarTextStyle: TopBarTextStyle,
    private val title: String? = null,
) : NavigationControlsHandler {

    override val resources: Resources
        get() = fragment.resources

    private val showBackButton: Boolean = fragment.shouldShowBackButton()
    private val showCloseButton: Boolean = fragment.shouldShowCloseButton()

    override fun setup() {
        with(toolbar) {
            setupToolbar(
                lifecycleScope = fragment.viewLifecycleOwner.lifecycleScope,
                title = title,
                topBarColor = topBarColor,
                onBackClickListener = NavigationControlsHandler.DefaultBackPressedListener(fragment.activity),
                onCloseClickListener = NavigationControlsHandler.DefaultCloseClickListener(fragment),
                topBarTextStyle = topBarTextStyle,
            )

            if (!showBackButton) {
                hideBackButton()
            }

            if (!showCloseButton) {
                hideCloseButton()
            }

            if (showBackButton || showCloseButton) {
                visibility = View.VISIBLE
            }
        }
    }
}
