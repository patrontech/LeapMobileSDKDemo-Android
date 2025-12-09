package com.greencopper.interfacekit.interests

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.interests.ui.InterestsFragment
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.testmocks.mockBundleConstructor
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class InterestsPickerInitializerTest {

    init {
        Toolkit.setupTest()
        mockBundleConstructor()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    private val initializer = InterestsPickerInitializer()
    val params = InterestsData("title", "subtitle", ScreenNameAnalytics("interests"))

    @Test
    fun givenParams_getLayout_returnsInterestsFragment() {
        val result = initializer.getLayout(params.encodeToJsonElement())
        assertThat(result).isInstanceOf(InterestsFragment::class.java)
    }

    @Test
    fun givenParams_getRedirectionHash_returnsRedirectionHash() {
        val result = initializer.redirectionHashFor(params.encodeToJsonElement())
        assertThat(result.featureKey).isEqualTo(InterestsPickerInitializer.key)
        assertThat(result.identifier).isEqualTo(params.analytics.screenName)
    }
}
