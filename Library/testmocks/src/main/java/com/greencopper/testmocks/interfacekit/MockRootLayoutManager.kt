package com.greencopper.testmocks.interfacekit

import androidx.fragment.app.FragmentManager
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.rootview.RootLayoutManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockRootLayoutManager : RootLayoutManager {

    public var setupRootLayoutCalled: Boolean = false
    public var updateRootLayoutCalled: Boolean = false

    override fun setupRootLayout(
        fragmentManager: FragmentManager,
        alreadySetup: Boolean
    ): Flow<Layout> {
        setupRootLayoutCalled = true
        return flowOf()
    }

    override suspend fun updateRootLayout() {
        updateRootLayoutCalled = true
    }
}
