package com.greencopper.interfacekit.rootview

import androidx.activity.OnBackPressedDispatcher
import com.greencopper.interfacekit.navigation.layout.Layout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public class RootLayoutHolder {
    public fun setRootLayout(layout: Layout) {
        _rootLayoutHolder.value = layout
        onBackPressDispatcher = OnBackPressedDispatcher()
    }

    internal fun clearRootLayout() {
        _rootLayoutHolder.value = null
    }

    public companion object {
        private val _rootLayoutHolder: MutableStateFlow<Layout?> = MutableStateFlow(null)
        public val rootLayoutHolder: StateFlow<Layout?> = _rootLayoutHolder.asStateFlow()
        public var onBackPressDispatcher: OnBackPressedDispatcher? = null
    }
}
