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

internal class InterestsOnboardingInitializerTest {

    init {
        Toolkit.setupTest()
        mockBundleConstructor()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    val initializer = InterestsOnboardingInitializer()
    val params = InterestsData("title", "subtitle", ScreenNameAnalytics("interests"))

    @Test
    fun givenParams_resolveWithParams_returnsInterestsFragment() {
        val result = initializer.resolveWithParams(params, "pageId")
        assertThat(result).isInstanceOf(InterestsFragment::class.java)
    }

    @Test
    fun givenParams_resolveWithJson_returnsInterestsFragment() {
        val result = initializer.resolve(params.encodeToJsonElement(), "pageId")
        assertThat(result).isInstanceOf(InterestsFragment::class.java)
    }

    @Test
    fun showInSequence_returnsTrue() {
        val result = initializer.showInSequence()
        assertThat(result).isTrue
    }
}
