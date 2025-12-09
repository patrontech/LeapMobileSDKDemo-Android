package com.greencopper.interfacekit.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/** Activity binding delegate, may be used since onCreate up to onDestroy (inclusive) */
public inline fun <T : ViewBinding> AppCompatActivity.viewBinding(crossinline factory: (LayoutInflater) -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE) {
        factory(layoutInflater)
    }

/** Fragment binding delegate, may be used since onViewCreated up to onDestroyView (inclusive) */
public fun <T : ViewBinding> DialogFragment.viewBinding(factory: (LayoutInflater) -> T): ReadOnlyProperty<DialogFragment, T> =
    object : ReadOnlyProperty<DialogFragment, T>, DefaultLifecycleObserver {
        private var binding: T? = null

        override fun getValue(thisRef: DialogFragment, property: KProperty<*>): T =
            binding ?: factory(layoutInflater).also {
                // if binding is accessed after Lifecycle is DESTROYED, create new instance, but don't cache it
                if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                    viewLifecycleOwner.lifecycle.addObserver(this)
                    binding = it
                }
            }

        override fun onDestroy(owner: LifecycleOwner) {
            binding = null
        }
    }

/** Not really a delegate, just a small helper for RecyclerView.ViewHolders */
public inline fun <T : ViewBinding> ViewGroup.viewBinding(factory: (LayoutInflater, ViewGroup, Boolean) -> T): T =
    factory(LayoutInflater.from(context), this, false)