package com.greencopper.interfacekit

import android.view.Gravity
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.IdRes
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import androidx.transition.Slide
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.ui.fragment.getStackTag
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.shouldShowBackButton
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.shouldShowCloseButton
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.d
import kotlinx.coroutines.launch

public fun FragmentManager.replace(
    @IdRes containerLayoutId: Int,
    fragment: Fragment,
    tag: String? = null,
    addToBackStack: Boolean = false,
) {
    commit(allowStateLoss = true) {
        val animation = Fade()
        fragment.enterTransition = animation
        fragment.returnTransition = animation
        fragment.exitTransition = animation

        replace(containerLayoutId, fragment, tag)
        if (addToBackStack) addToBackStack(tag)
    }
    App.log.d("Feature replace: ${fragment.getStackTag()}")
}

public fun FragmentManager.replaceBackStackAware(
    @IdRes containerLayoutId: Int,
    origin: Fragment,
    fragment: Fragment,
) {
    val shouldAddToBackStack = backStackEntryCount > 0

    fragment.lifecycleScope.launch {
        commit(allowStateLoss = true) {

            val fadeAnimation = Fade()
            origin.exitTransition = fadeAnimation

            if (shouldAddToBackStack) {
                popBackStackImmediate()
            }

            fragment.enterTransition = fadeAnimation
            val exitAnimation = when {
                fragment.shouldShowBackButton() -> Slide(Gravity.END).apply {
                    interpolator = AccelerateInterpolator()
                }
                fragment.shouldShowCloseButton() -> Slide(Gravity.BOTTOM).apply {
                    interpolator = AccelerateInterpolator()
                }
                else -> fadeAnimation
            }

            fragment.returnTransition = exitAnimation
            fragment.exitTransition = Fade(Fade.OUT).apply {
                interpolator = DecelerateInterpolator()
            }

            replace(containerLayoutId, fragment)
            if (shouldAddToBackStack) {
                addToBackStack(fragment.getStackTag())
            }
        }
    }
    App.log.d("Feature replaceBackStackAware: ${fragment.getStackTag()}")
}

public fun FragmentManager.push(@IdRes containerLayoutId: Int, fragment: Fragment) {
    commit(allowStateLoss = true) {
        if (isAddToBackStackAllowed) {
            val animationEnter = Slide(Gravity.END).apply {
                interpolator = DecelerateInterpolator()
            }
            val animationExit = Slide(Gravity.END).apply {
                interpolator = AccelerateInterpolator()
            }
            fragment.enterTransition = animationEnter
            fragment.returnTransition = animationExit
            fragment.exitTransition = Slide(Gravity.START).apply {
                interpolator = DecelerateInterpolator()
            }

            replace(containerLayoutId, fragment)
            addToBackStack(fragment.getStackTag())
        }
    }
    App.log.d("Feature push: ${fragment.getStackTag()}")
}

public fun FragmentManager.present(
    @IdRes containerLayoutId: Int,
    fragment: Layout,
    route: Route.Present
) {
    present(containerLayoutId, fragment, route.feature.key.toString())
}

/**
 * Should be called on the Activity supportFragmentManager or NavController parentFragmentManager
 */
public fun FragmentManager.present(@IdRes containerLayoutId: Int, fragment: Layout, tag: String) {

    commit(allowStateLoss = true) {
        if (isAddToBackStackAllowed) {
            val animationEnter = Slide(Gravity.BOTTOM).apply {
                interpolator = DecelerateInterpolator()
            }
            val animationExit = Slide(Gravity.BOTTOM).apply {
                interpolator = AccelerateInterpolator()
            }
            fragment.enterTransition = animationEnter
            fragment.returnTransition = animationExit
            fragment.exitTransition = Fade(Fade.OUT).apply {
                interpolator = DecelerateInterpolator()
            }

            replace(containerLayoutId, fragment, tag)
            addToBackStack(fragment.getStackTag())
        }
    }
    App.log.d("Feature present: ${fragment.getStackTag()}")
}

/**
 * Should be called on the Activity supportFragmentManager or NavController parentFragmentManager
 */
public fun FragmentManager.presentBottomSheet(@IdRes containerLayoutId: Int, fragment: Layout, key: String) {

    commit(allowStateLoss = true) {
        if (isAddToBackStackAllowed) {
            val animationEnter = Slide(Gravity.BOTTOM).apply {
                interpolator = DecelerateInterpolator()
            }
            val animationExit = Slide(Gravity.BOTTOM).apply {
                interpolator = AccelerateInterpolator()
            }
            fragment.enterTransition = animationEnter
            fragment.returnTransition = animationExit
            fragment.exitTransition = animationExit

            add(containerLayoutId, fragment, key)
        }
    }
    App.log.d("Feature present: ${fragment.getStackTag()}")
}

internal fun FragmentManager.popBackStackIfPossible(): Boolean = when {
    backStackEntryCount > 0 -> {
        popBackStack()
        true
    }
    else -> false
}

public fun FragmentManager.findFragmentByTagInStack(tag: String): Fragment? {
    val fragment = findFragmentByTag(tag)
    if (fragment != null) return fragment

    fragments.forEach {
        val childFragment = it.childFragmentManager.findFragmentByTagInStack(tag)
        if (childFragment != null) {
            return childFragment
        }
    }

    return null
}
