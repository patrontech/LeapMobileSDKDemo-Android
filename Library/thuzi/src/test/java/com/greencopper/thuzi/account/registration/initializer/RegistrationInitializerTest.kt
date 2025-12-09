package com.greencopper.thuzi.account.registration.initializer

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.thuzi.account.registration.model.RegistrationConfiguration
import com.greencopper.thuzi.account.registration.model.RegistrationLayoutData
import com.greencopper.thuzi.account.registration.recipe.RegistrationConfigurationHolder
import com.greencopper.thuzi.account.registration.ui.RegistrationFragment
import com.greencopper.thuzi.setAuthenticated
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RegistrationInitializerTest {

    private val registrationConfigHolder = RegistrationConfigurationHolder()
    private val localStorage: LocalStorage
    private val initializer: RegistrationInitializer
    private val registrationAnalytics: ScreenNameAnalytics

    init {
        Toolkit.setupTest()
        localStorage = App.resolve()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())

        registrationConfigHolder.currentConfiguration.value =
            RegistrationConfiguration("", "","", "", "", "", "", ScreenNameAnalytics(""), "")
        initializer = RegistrationInitializer(
            localStorage,
            registrationConfigHolder,
        )
        registrationAnalytics = ScreenNameAnalytics("")
    }

    @Test
    fun whenGettingLayout_withoutParams_shouldGetLayout() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(
                null
            )
        }
    }

    @Test
    fun whenGettingLayout_withParams_shouldGetLayout() {
        mockBundleConstructor()
        val params = RegistrationData(null).encodeToJsonElement()
        val layout = initializer.getLayout(params)
        assertThat(layout).isInstanceOf(RegistrationFragment::class.java)
    }

    @Test
    fun whenGettingLayout_withParams_registrationUrlSpecified_shouldGetLayout() {
        mockBundleConstructor()
        val params = RegistrationData(null, registrationUrl = "").encodeToJsonElement()
        val layout = initializer.getLayout(params)
        assertThat(layout).isInstanceOf(RegistrationFragment::class.java)
    }

    @Test
    fun whenGettingLayout_withParams_deviceLinkingUrlNotSpecified_shouldGetLayout() {
        registrationConfigHolder.currentConfiguration.value = RegistrationConfiguration(
            "",
            "",
            null,
            "",
            "",
            "",
            "",
            ScreenNameAnalytics(""),
            "",
        )
        mockBundleConstructor()
        val params = RegistrationData(null).encodeToJsonElement()
        val layout = initializer.getLayout(params)
        assertThat(layout).isInstanceOf(RegistrationFragment::class.java)
    }

    @Test
    fun whenGettingLayout_withParams_deviceLinkingUrlNotSpecifiedButRegistrationUrlSpecified_shouldGetLayout() {
        registrationConfigHolder.currentConfiguration.value = RegistrationConfiguration(
            "",
            "",
            null,
            "",
            "",
            "",
            "",
            ScreenNameAnalytics(""),
            "",
        )
        mockBundleConstructor()
        val params = RegistrationData(null, registrationUrl = "").encodeToJsonElement()
        val layout = initializer.getLayout(params)
        assertThat(layout).isInstanceOf(RegistrationFragment::class.java)
    }

    @Test
    fun whenGettingLayout_withWrongParams_shouldThrow() {
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(JsonNull)
        }
    }

    @Test
    fun whenGettingLayout_Authenticated_shouldThrow() {
        setAuthenticated(localStorage, true)
        val params = RegistrationData().encodeToJsonElement()
        assertThrows<AlreadyRegisteredException> {
            initializer.getLayout(params)
        }
    }

    @Test
    fun whenGettingLayout_withRegistrationUrl_shouldGetLayout() {
        mockBundleConstructor()
        val params = RegistrationData(registrationUrl = "https:\\test.com").encodeToJsonElement()
        val layout = initializer.getLayout(params)
        assertThat(layout).isInstanceOf(RegistrationFragment::class.java)
    }

    @Test
    fun whenGettingLayout_whenConfigurationNull_shouldThrow() {
        registrationConfigHolder.currentConfiguration.value = null
        val params = RegistrationData(registrationUrl = "https:\\test.com").encodeToJsonElement()
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(params)
        }
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetHash() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(RegistrationInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withParams_shouldGetHash() {
        val featureKey = FeatureKey("test", 1)
        val redirectionHash = RedirectionHash(featureKey)
        val params = RegistrationData(redirectionHash = RedirectionHash(featureKey)).encodeToJsonElement()
        assertThat(initializer.redirectionHashFor(params)).isEqualTo(redirectionHash)
    }

    @Test
    fun whenGettingRedirectionHash_whenConfigurationNull_shouldGetHash() {
        registrationConfigHolder.currentConfiguration.value = null
        val params = RegistrationData().encodeToJsonElement()
        val redirectionHash = initializer.redirectionHashFor(params)
        val expectedRedirectionHash = RedirectionHash(FeatureKey("Thuzi.Registration", 1))
        assertThat(redirectionHash).isEqualTo(expectedRedirectionHash)
    }

    @Test
    fun whenGettingFeatureParamsAnalytics_withParams_shouldGetAnalytics() {
        val analytics = ScreenNameAnalytics("screenName")
        val params = RegistrationLayoutData.Params(analytics)
        val paramsJson = App.resolve<Json>().encodeToJsonElement(params)
        val featureInfo = FeatureInfo(FeatureKey("", 0), paramsJson)
        val registrationLayoutData = RegistrationLayoutData(
            "",
            "",
            "",
            "",
            "",
            featureInfo,
            registrationAnalytics,
            OnboardingPageLayoutData("pageId"),
            initializer.redirectionHashFor(null)
        )
        assertThat(registrationLayoutData.getFeatureParamsAnalytics()).isEqualTo(analytics)
    }

    @Test
    fun whenGettingFeatureParamsAnalytics_withoutFeature_shouldGetNull() {
        val registrationLayoutData = RegistrationLayoutData(
            "",
            "",
            "",
            "",
            "",
            null,
            registrationAnalytics,
            OnboardingPageLayoutData("pageId"),
            initializer.redirectionHashFor(null)
        )
        assertThat(registrationLayoutData.getFeatureParamsAnalytics()).isNull()
    }

    @Test
    fun whenGettingFeatureParamsAnalytics_withoutParams_shouldGetNull() {
        val featureInfo = FeatureInfo(FeatureKey("", 0))
        val registrationLayoutData = RegistrationLayoutData(
            "",
            "",
            "",
            "",
            "",
            featureInfo,
            registrationAnalytics,
            OnboardingPageLayoutData("pageId"),
            initializer.redirectionHashFor(null)
        )
        assertThat(registrationLayoutData.getFeatureParamsAnalytics()).isNull()
    }

    @Test
    fun whenGettingFeatureParamsAnalytics_whenParamsIncorrect_shouldGetNull() {
        val featureInfo = FeatureInfo(
            key = FeatureKey("", 0),
            params = JsonNull,
        )
        val registrationLayoutData = RegistrationLayoutData(
            "",
            "",
            "",
            "",
            "",
            featureInfo,
            registrationAnalytics,
            OnboardingPageLayoutData("pageId"),
            initializer.redirectionHashFor(null)
        )
        assertThat(registrationLayoutData.getFeatureParamsAnalytics()).isNull()
    }

    @Test
    fun showInSequence_returnsTrue() {
        assertThat(initializer.showInSequence()).isTrue
    }
}
