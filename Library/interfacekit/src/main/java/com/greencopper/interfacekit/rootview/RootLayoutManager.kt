package com.greencopper.interfacekit.rootview

import androidx.fragment.app.FragmentManager
import com.greencopper.interfacekit.navigation.layout.Layout
import kotlinx.coroutines.flow.Flow

public interface RootLayoutManager {
    public fun setupRootLayout(fragmentManager: FragmentManager, alreadySetup: Boolean): Flow<Layout>
    public suspend fun updateRootLayout()
}

