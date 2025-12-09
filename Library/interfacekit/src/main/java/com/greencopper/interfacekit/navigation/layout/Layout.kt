package com.greencopper.interfacekit.navigation.layout

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.greencopper.interfacekit.navigation.NavigationController
import com.greencopper.interfacekit.navigation.PresentedFragment

public typealias Layout = DialogFragment

public fun Layout.getNavigationController(): NavigationController<*>? =
    getClosestNavigationController(parentFragment)

public fun Layout.getParentNavigationController(): NavigationController<*>? =
    getClosestNavigationController(getNavigationController()?.ncParentFragment)

private fun getClosestNavigationController(parent: Fragment?): NavigationController<*>? {
    return if (parent == null || parent is NavigationController<*>) {
        parent as? NavigationController<*>
    } else {
        getClosestNavigationController(parent.parentFragment)
    }
}

public fun Layout.getParentPresentedFragment(): Layout? =
    getClosestPresentedFragment(parentFragment)


private fun getClosestPresentedFragment(parent: Fragment?): Layout? {
    return if (parent == null || parent is PresentedFragment) {
        parent as? PresentedFragment
    } else {
        getClosestPresentedFragment(parent.parentFragment)
    }
}

