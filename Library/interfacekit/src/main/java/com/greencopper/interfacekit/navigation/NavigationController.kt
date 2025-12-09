package com.greencopper.interfacekit.navigation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import com.greencopper.interfacekit.*
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.rootview.RootLayoutHolder
import com.greencopper.interfacekit.ui.fragment.BottomSheetDialogFragmentContainer
import com.greencopper.interfacekit.ui.fragment.getStackTag

public interface NavigationController<T : Layout> {

    @Suppress("UNCHECKED_CAST")
    public val ncChildFragmentManager: FragmentManager
        get() = (this as T).childFragmentManager

    @Suppress("UNCHECKED_CAST")
    public val ncParentFragmentManager: FragmentManager
        get() = (this as T).parentFragmentManager

    @Suppress("UNCHECKED_CAST")
    public val ncParentFragment: Fragment?
        get() = (this as T).parentFragment

    @IdRes
    public fun getContainerId(): Int

    public fun push(layout: Layout) {
        ncChildFragmentManager.push(getContainerId(), layout)
    }

    @Suppress("UNCHECKED_CAST")
    public fun present(layout: Layout, onAttach: () -> Unit = {}) {
        (RootLayoutHolder.rootLayoutHolder.value as? NavigationController<*>)?.let { rootNavigationFragment ->
            if ((rootNavigationFragment as? T)?.isAdded == true) {
                presentOnRootAttached(rootNavigationFragment, layout, onAttach)
            } else {
                val onAttachListener: FragmentOnAttachListener = oneTimeFragmentOnAttachListener { _, _ ->
                    presentOnRootAttached(rootNavigationFragment, layout, onAttach)
                }
                rootNavigationFragment.ncParentFragmentManager.addFragmentOnAttachListener(
                    onAttachListener
                )
            }
        }
    }

    /**
     * This is made to wait for the RootFragment (e.g. TabBarFragment) before presenting something else above
     */
    private fun presentOnRootAttached(
        rootNavigationFragment: NavigationController<*>,
        layout: Layout,
        onAttach: () -> Unit = {}
    ) {
        if (rootNavigationFragment.ncChildFragmentManager.fragments.isNotEmpty()) {
            presentLayoutWhenIsReady(layout, rootNavigationFragment, onAttach)
        } else {
            val onAttachListener: FragmentOnAttachListener = oneTimeFragmentOnAttachListener { _, _ ->
                presentLayoutWhenIsReady(layout, rootNavigationFragment, onAttach)
            }
            rootNavigationFragment.ncChildFragmentManager.addFragmentOnAttachListener(onAttachListener)
        }
    }

    private fun presentLayoutWhenIsReady(
        layout: Layout,
        rootNavigationFragment: NavigationController<*>,
        onAttach: () -> Unit = {}
    ) {
        val presentedFragment = PresentedFragment()

        var onAttachListener: FragmentOnAttachListener? = null
        onAttachListener = FragmentOnAttachListener { fragmentManager, fragment ->
            if (fragment === presentedFragment) {
                onAttachListener?.let { fragmentOnAttachListener ->
                    fragmentManager.removeFragmentOnAttachListener(fragmentOnAttachListener)
                }
                presentedFragment.childFragmentManager.addFragmentOnAttachListener { _, _ -> onAttach() }
                presentedFragment.replace(layout)
            }
        }
        rootNavigationFragment.ncChildFragmentManager.present(
            rootNavigationFragment.getContainerId(),
            presentedFragment,
            layout.getStackTag()
        )
        presentedFragment.parentFragmentManager.addFragmentOnAttachListener(onAttachListener)
    }

    public fun bottomSheetPresent(
        layout: Layout,
        backgroundColor: Int,
    ) {
        val bottomSheetDialogFragmentContainer = BottomSheetDialogFragmentContainer()
        bottomSheetDialogFragmentContainer.arguments = Bundle().apply {
            putInt(BottomSheetDialogFragmentContainer.EXTRA_BACKGROUND_COLOR, backgroundColor)
        }

        ncParentFragmentManager.addFragmentOnAttachListener(oneTimeFragmentOnAttachListener { _, _ ->
            bottomSheetDialogFragmentContainer.replace(layout)
        })

        ncParentFragmentManager.presentBottomSheet(
            0,
            bottomSheetDialogFragmentContainer,
            bottomSheetDialogFragmentContainer.getStackTag()
        )
        if (this is BottomSheetDialogFragmentContainer) dismiss()
    }

    @Suppress("UNCHECKED_CAST")
    public fun replace(layout: Layout, tag: String? = null) {
        if ((this as? T)?.isAdded == true) {
            unsafeReplace(layout, tag)
        } else {
            val onAttachListener: FragmentOnAttachListener = oneTimeFragmentOnAttachListener { _, _ ->
                unsafeReplace(layout, tag)
            }
            ncParentFragmentManager.addFragmentOnAttachListener(onAttachListener)
        }
    }

    public fun replaceBackStackAware(origin: Layout, addedLayout: Layout) {
        ncChildFragmentManager.replaceBackStackAware(getContainerId(), origin, addedLayout)
    }

    private fun unsafeReplace(layout: Layout, tag: String?) {
        ncChildFragmentManager.replace(
            containerLayoutId = getContainerId(),
            fragment = layout,
            tag = tag,
        )
    }
}
