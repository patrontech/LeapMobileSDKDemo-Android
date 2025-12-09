package com.greencopper.interfacekit.onboarding.maincard

import android.os.Bundle
import com.greencopper.core.data.putKiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.fullscreenmedia.FullScreenMediaData
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.onboarding.initializers.ParameterizedOnboardingPageInitializer.ParameterizedOnboardingInitializerException
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class MainActionCardPageInitializerTest {

    private lateinit var classUnderTest: MainActionCardPageInitializer

    private val validParams = MainActionCardData(
        title = "title",
        text = "text",
        backgroundImage = "image",
        OnboardingPageLayoutData.OnboardingAnalytics(
            screenName = "screenName",
            featureName = "featureName"
        )
    )

    init {
        Toolkit.setupTest()
    }

    @BeforeEach
    fun setupEach() {
        classUnderTest =
            MainActionCardPageInitializer()
    }

    @Test
    @DisplayName("Given params are valid, When resolve is called, Then a layout is returned")
    fun resolveShouldSucceed() {
        mockkStatic("com.greencopper.core.data.KiboSerializableKt")
        every {
            any<Bundle>().putKiboSerializable(
                any(),
                any<MainActionCardData>()
            )
        } returns Bundle()
        mockBundleConstructor()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
        assertThat(classUnderTest.resolve(validParams.encodeToJsonElement(), "id")).isNotNull
    }

    @Test
    @DisplayName("Given params are null, When resolve is called, Then NoParametersProvidedError is thrown")
    fun resolveShouldThrowNoParametersProvidedError() {
        val exception =
            assertThrows<ParameterizedOnboardingInitializerException.NoParametersProvidedError> {
                classUnderTest.resolve(null, "id")
            }
        assertThat(exception.message).isNotNull
    }

    @Test
    @DisplayName("Given params are not deserializable, When resolve is called, Then DecodingFailedException is thrown")
    fun resolveShouldThrowDecodingFailedException() {
        val exception =
            assertThrows<ParameterizedOnboardingInitializerException.DecodingFailedException> {
                classUnderTest.resolve(JsonNull, "id")
            }
        assertThat(exception.message).isNotNull
    }

    @Test
    @DisplayName("Given params are not invalid, When resolve is called, Then InvalidParametersException is thrown")
    fun resolveWithInvalidParamsShouldThrowDecodingFailedException() {
        val exception =
            assertThrows<ParameterizedOnboardingInitializerException.DecodingFailedException> {
                val params = FullScreenMediaData(
                    "path",
                    ScreenNameAnalytics("analytics")
                ).encodeToJsonElement()
                classUnderTest.resolve(params, "id")
            }
        assertThat(exception.message).isNotNull
    }

    @Test
    fun showInSequence_returnsTrue() {
        assertThat(classUnderTest.showInSequence()).isTrue
    }
}
