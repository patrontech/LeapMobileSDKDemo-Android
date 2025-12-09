package com.greencopper.thuzi.microsite

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.thuzi.microsite.ui.MicrositeFragment
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.unmockkAll
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class MicrositeInitializerTest {
    private val localStorage: LocalStorage
    private val localizationService: LocalizationService = MockLocalizationService()

    init {
        Toolkit.setupTest()
        localStorage = App.resolve()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    private val initializer = MicrositeInitializer(
        localStorage,
        localizationService,
    )

    @Test
    fun whenGettingLayout_withoutParams_whenRegistered_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenGettingLayout_withWrongParams_whenRegistered_shouldThrow() {
        val params = JsonNull
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(params)
        }
    }

    @Test
    fun whenGettingLayout_whenRegisteredAndCorrectUrl_shouldGetMicrositeLayout() {
        mockBundleConstructor()
        val analytics = ScreenNameAnalytics("Thuzi Microsite")
        val micrositeData = MicrositeData(
            "https://public.events.thuzi.com/5f0e1d6dab31e838d0d8bb6e/events/5f19f6e83299546cd0c4bb12/attendee/18B4B-6DA53-F4187",
            analytics
        )
        val featureParams: FeatureParams = micrositeData.encodeToJsonElement()
        val layout = initializer.getLayout(featureParams) as? MicrositeFragment
        assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingLayout_whenRegisteredAndIncorrectUrl_shouldGetMicrositeLayout() {
        mockBundleConstructor()
        val analytics = ScreenNameAnalytics("Thuzi Microsite")
        val micrositeData = MicrositeData(
            "incorrect url",
            analytics
        )
        val featureParams: FeatureParams = micrositeData.encodeToJsonElement()
        val layout = initializer.getLayout(featureParams) as? MicrositeFragment
        assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(JsonNull)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(MicrositeInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withWrongParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(JsonNull)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(MicrositeInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withProperParams_shouldGetHash() {
        val data = MicrositeData("https://someUrl.com", ScreenNameAnalytics("Microsite"))
        val params = data.encodeToJsonElement()

        val redirectionHash = initializer.redirectionHashFor(params)
        assertThat(redirectionHash).isNotNull
    }

    @AfterEach
    fun reset() {
        unmockkAll()
    }
}
