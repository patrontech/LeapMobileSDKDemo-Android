package com.greencopper.interfacekit.ui.views.navigationcontrols.handlers

import android.content.res.Resources
import android.view.View
import com.greencopper.interfacekit.color.TopBarColor
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.views.navigationcontrols.NavigateBackButton
import com.greencopper.interfacekit.ui.views.navigationcontrols.NavigateCloseButton

public open class DefaultButtonsNavigationControlsHandler(
    private val fragment: Layout,
    private val backButton: NavigateBackButton,
    private val closeButton: NavigateCloseButton,
    private val topBarColor: TopBarColor
): NavigationControlsHandler {

    override val resources: Resources
        get() = fragment.resources

    protected val showBackButton: Boolean = fragment.shouldShowBackButton()
    protected val showCloseButton: Boolean = fragment.shouldShowCloseButton()

    override fun setup() {
        backButton.setupButton(
            topBarColor,
            NavigationControlsHandler.DefaultBackPressedListener(fragment.activity)
        )
        closeButton.setupButton(
            topBarColor
        ) {
            fragment.closePresentedLayout()
        }

        if (showBackButton) {
            backButton.visibility = View.VISIBLE
        }
        if (showCloseButton) {
            closeButton.visibility = View.VISIBLE
        }
    }
}
