package com.greencopper.interfacekit.navigation

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.rootview.RootLayoutHolder
import com.greencopper.interfacekit.tabBar.TabBarLayoutData
import com.greencopper.interfacekit.tabBar.ui.TabBarFragment
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.testmocks.interfacekit.mockRedirectionHash
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class NavigationControllerTest {
    private val context = InstrumentationRegistry.getInstrumentation().context

    init {
        Toolkit.setupTest(applicationContext = context)
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    @Test
    fun tabBarFragment_hasContainerId_success() {
        val params = TabBarLayoutData(0, true, emptyList(), mockRedirectionHash)
        val tabBarFragment: NavigationController<TabBarFragment> = TabBarFragment(params)
        assertThat(tabBarFragment.getContainerId()).isGreaterThan(0)
    }

    @Test
    fun navigationFragment_hasContainerId_success() {
        val tabBarFragment: NavigationController<NavigationFragment> = NavigationFragment()
        assertThat(tabBarFragment.getContainerId()).isGreaterThan(0)
    }

    @Test
    fun presentShouldSucceed() {
        val scenario = launchFragmentInContainer(bundleOf()) {
            NavigationFragment()
        }
        scenario.withFragment {
            RootLayoutHolder().setRootLayout(this as Layout)
            assertDoesNotThrow {
                this.present(NavigationFragment())
            }
        }

    }
}
