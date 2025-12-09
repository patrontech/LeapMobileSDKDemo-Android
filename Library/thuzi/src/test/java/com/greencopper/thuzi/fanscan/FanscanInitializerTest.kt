package com.greencopper.thuzi.fanscan

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.thuzi.fanscan.ui.fragment.FanscanFragment
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.recipe.RegistrationConfigurationHolder
import com.greencopper.thuzi.setup
import com.greencopper.thuzi.teardown
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.*

internal class FanscanInitializerTest {

    private val initializer: FanscanInitializer

    private val localStorage: LocalStorage
    private val registrationConfigHolder = RegistrationConfigurationHolder()

    private val fanscanData = FanscanData(
        "https://api.events.thuzi.com/api/brand/5dd455d479fdde4de4bd2a41/event/5fc15d1360733d1340cf9cb3/checkin/",
        ScreenNameAnalytics("Thuzi Fanscan"),
        SuccessPage("Test Button", "https://google.com")
    )
    private val correctFanscanParameters: JsonElement

    init {
        Toolkit.setupTest()
        mockBundleConstructor()
        bindSingleton(registrationConfigHolder)

        localStorage = App.resolve()
        correctFanscanParameters = fanscanData.encodeToJsonElement()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())

        initializer = FanscanInitializer()
    }

    @Test
    fun whenGettingLayout_withoutParams_shouldGetLayout() {
        registrationConfigHolder.setup()
        val layout = initializer.getLayout(correctFanscanParameters)
        assertThat(layout).isNotNull
        registrationConfigHolder.teardown()
    }

    @Test
    fun whenGettingLayout_withoutParams_LoggedIn_shouldThrow() {
        val date = LocalDateTime.now().plusYears(1)

        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.of(date, ZoneId.systemDefault()).toString()
        localStorage.project.thuzi.jwt.value = "123"

        registrationConfigHolder.setup()

        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenGettingLayout_withWrongParameters_shouldThrow() {
        val date = LocalDateTime.now().plusYears(1)
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.of(date, ZoneId.systemDefault()).toString()
        localStorage.project.thuzi.jwt.value = "123"
        registrationConfigHolder.setup()

        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(JsonPrimitive("123"))
        }
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetHash() {
        val redirectionHash = initializer.redirectionHashFor(correctFanscanParameters)
        assertThat(redirectionHash.featureKey)
            .isEqualTo(FeatureKey("Thuzi.Fanscan", 1))
    }

    @Test
    fun whenGettingRedirectionHash_withWrongParams_shouldGetHash() {
        val redirectionHash = initializer.redirectionHashFor(JsonPrimitive("123"))
        assertThat(redirectionHash.featureKey)
            .isEqualTo(FeatureKey("Thuzi.Fanscan", 1))
    }

    @Test
    fun whenGettingRedirectionHash_withNullParams_shouldGetHash() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash.featureKey)
            .isEqualTo(FeatureKey("Thuzi.Fanscan", 1))
    }

    @Test
    fun whenGettingLayout_whenRegistered_shouldGetFanscanLayout() {
        val date = LocalDateTime.now().plusYears(1)
        val registered = "Registered"
        localStorage.project.thuzi.apply {
            jwt.value = registered
            jwtExpirationDate.value = ZonedDateTime.of(date, ZoneId.systemDefault()).toString()
            attendeeId.value = registered
            qrCode.value = registered
            userFirstName.value = registered
        }
        val layout = initializer.getLayout(correctFanscanParameters)
        assertThat(layout is FanscanFragment).isTrue
    }

    @Test
    fun testSerializable_FanScanData_All() = testKiboSerializable(fanscanData)

    @Test
    fun testSerializable_FanScanData_WithoutSuccessConfig() = testKiboSerializable(
        FanscanData(
            "https://api.events.thuzi.com/api/brand/5dd455d479fdde4de4bd2a41/event/5fc15d1360733d1340cf9cb3/checkin/",
            ScreenNameAnalytics("Thuzi Fanscan")
        )
    )
}
