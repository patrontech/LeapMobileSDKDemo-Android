package com.greencopper.event.reminders

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RemindersInitializerTest {

    private val initializer = RemindersInitializer()

    private val params = RemindersData(
        analytics = ScreenNameAnalytics("screenName")
    )

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    @Test
    fun whenGettingLayout_withoutParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenGettingLayout_withEmptyParams_shouldThrow() {
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(buildJsonObject { put("testKey", "testValue") })
        }
    }

    @Test
    fun whenGettingLayout_withProperParams_shouldGetLayout() {
        mockBundleConstructor()
        val layout = initializer.getLayout(params.encodeToJsonElement())
        Assertions.assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(null)
        Assertions.assertThat(redirectionHash)
            .isEqualTo(RedirectionHash(RemindersInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withProperParams_shouldGetHash() {
        val params = App.resolve<Json>().encodeToJsonElement(
            RemindersData.serializer(), params
        )
        val redirectionHash = initializer.redirectionHashFor(params)
        Assertions.assertThat(redirectionHash).isNotNull
    }
}
