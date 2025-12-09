package com.greencopper.thuzi.logout

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.testmocks.mockBundleConstructor
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.logout.ui.LogoutFragment
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


internal class LogoutInitializerTest {

    init {
        Toolkit.setupTest()
        mockBundleConstructor()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    private val initializer = LogoutInitializer()
    private val params = LogoutLayoutData(ScreenNameAnalytics("logoutScreen"))

    @Test
    fun givenParams_getLayout_returnsInterestsFragment() {
        val result = initializer.getLayout(params.encodeToJsonElement())
        assertThat(result).isInstanceOf(LogoutFragment::class.java)
    }

    @Test
    fun givenParams_getRedirectionHash_returnsRedirectionHash() {
        val result = initializer.redirectionHashFor(params.encodeToJsonElement())
        assertThat(result.featureKey).isEqualTo(LogoutInitializer.key)
        assertThat(result.identifier).isEqualTo(params.analytics.screenName)
    }
}
