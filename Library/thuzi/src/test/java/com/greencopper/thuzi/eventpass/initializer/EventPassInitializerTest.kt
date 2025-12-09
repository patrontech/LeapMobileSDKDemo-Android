package com.greencopper.thuzi.eventpass.initializer

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.thuzi.eventpass.ui.EventPassFragment
import com.greencopper.thuzi.account.registration.recipe.RegistrationConfigurationHolder
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EventPassInitializerTest {

    init {
        Toolkit.setupTest()
        mockBundleConstructor()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    private val registrationConfigHolder = RegistrationConfigurationHolder()
    private val paramsJson = App.resolve<Json>().parseToJsonElement("{\"analytics\":{\"screenName\":\"Event Pass\"}}")

    private val initializer = EventPassInitializer()

    @Test
    fun whenGettingLayout_withoutParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenGettingLayout_withEmptyParams_shouldThrow() {
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(JsonNull)
        }
    }

    @Test
    fun whenGettingLayout_withProperParams_shouldGetLayout() {
        val eventPassData = EventPassData(ScreenNameAnalytics("Event Pass"))
        val params = eventPassData.encodeToJsonElement()
        val layout = initializer.getLayout(params)
        assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(EventPassInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withWrongParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(JsonNull)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(EventPassInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withProperParams_shouldGetHash() {
        val eventPassData = EventPassData(ScreenNameAnalytics("Event Pass"))
        val params = eventPassData.encodeToJsonElement()
        val redirectionHash = initializer.redirectionHashFor(params)
        assertThat(redirectionHash).isNotNull
    }

    @Test
    fun whenGettingLayout_whenRegistered_shouldGetEventPassLayout() {
        val layout = initializer.getLayout(paramsJson)
        assertThat(layout).isInstanceOf(EventPassFragment::class.java)
    }
}
