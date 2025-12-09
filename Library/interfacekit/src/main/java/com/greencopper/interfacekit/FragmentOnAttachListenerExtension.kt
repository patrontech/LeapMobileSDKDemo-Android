package com.greencopper.interfacekit

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener

public fun oneTimeFragmentOnAttachListener(block: (fragmentManager: FragmentManager, fragment: Fragment) -> Unit): FragmentOnAttachListener {
    var onAttachListener: FragmentOnAttachListener? = null
    onAttachListener = FragmentOnAttachListener { fragmentManager, fragment ->
        onAttachListener?.let { fragmentOnAttachListener ->
            fragmentManager.removeFragmentOnAttachListener(fragmentOnAttachListener)
        }
        block(fragmentManager, fragment)
    }
    return onAttachListener
}
