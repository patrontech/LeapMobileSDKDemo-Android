package com.greencopper.interfacekit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.tryResolve

internal class ViewModelFactory(
    vararg val args: Any?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return App.tryResolve<ViewModel>(tag = modelClass, args = *args) as T?
            ?: modelClass.newInstance()
    }
}