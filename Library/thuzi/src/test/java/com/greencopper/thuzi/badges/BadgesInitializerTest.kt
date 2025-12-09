package com.greencopper.thuzi.badges

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.thuzi.badges.initializer.BadgesData
import com.greencopper.thuzi.badges.initializer.BadgesInitializer
import com.greencopper.thuzi.badges.ui.BadgesFragment
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.recipe.RegistrationConfigurationHolder
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.*

internal class BadgesInitializerTest {

    private val localStorage: LocalStorage
    private val registrationConfigHolder = RegistrationConfigurationHolder()
    private val initializer = BadgesInitializer()

    init {
        Toolkit.setupTest()
        bindSingleton(registrationConfigHolder)
        mockBundleConstructor()
        localStorage = App.resolve()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())

        val expiration = LocalDateTime.now().plusYears(1)
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.of(expiration, ZoneId.systemDefault()).toString()
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
            initializer.getLayout(JsonNull)
        }
    }

    @Test
    fun whenGettingLayout_withProperParams_shouldGetLayout() {
        val badgesData = BadgesData(
            ScreenNameAnalytics("Badges"),
            "https://someUrl.com"
        )
        val params = badgesData.encodeToJsonElement()
        val layout = initializer.getLayout(params)
        assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(BadgesInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withWrongParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(JsonNull)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(BadgesInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withProperParams_shouldGetHash() {
        val badgesData = BadgesData(
            ScreenNameAnalytics("Badges"),
            "https://someUrl.com"
        )
        val params = badgesData.encodeToJsonElement()
        val redirectionHash = initializer.redirectionHashFor(params)
        assertThat(redirectionHash).isNotNull
    }

    @Test
    fun whenGettingLayout_whenRegistered_shouldGetBadgesLayout() {
        val badgesData = BadgesData(
            ScreenNameAnalytics("Thuzi Badges"),
            "https://public.events.thuzi.com/5f0e1d6dab31e838d0d8bb6e/events/5f19f6e83299546cd0c4bb12/attendee/18B4B-6DA53-F4187",
        )

        val layout = initializer.getLayout(badgesData.encodeToJsonElement())

        assertThat(layout).isInstanceOf(BadgesFragment::class.java)
    }
}
