package com.greencopper.interfacekit.navigation

import androidx.fragment.app.DialogFragment
import com.greencopper.interfacekit.R

public open class ContainerFragment : DialogFragment(R.layout.navigation_fragment) {
    // Can't use `containerId` because it conflicts with `getContainerId()`.
    public val containerFragmentId: Int = R.id.navigationFrameLayout
}

public open class NavigationFragment : ContainerFragment(),
    NavigationController<NavigationFragment> {

    override fun getContainerId(): Int = containerFragmentId
}

public open class PresentedFragment : NavigationFragment()
