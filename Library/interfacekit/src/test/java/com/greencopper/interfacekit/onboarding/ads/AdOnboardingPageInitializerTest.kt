package com.greencopper.interfacekit.onboarding.ads

import android.os.Bundle
import com.greencopper.core.data.putKiboSerializable
import com.greencopper.core.metrics.ItemNameIdAnalytics
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.onboarding.initializers.ParameterizedOnboardingPageInitializer.ParameterizedOnboardingInitializerException
import com.greencopper.interfacekit.onboarding.maincard.MainActionCardData
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockImageService
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.mockkStatic
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AdOnboardingPageInitializerTest {

    private val imageService = MockImageService().apply {
        isImageAvailable_result = true
    }
    private val initializer = AdOnboardingPageInitializer(imageService)

    private val validParams = AdOnboardingPageData(
        ads = listOf(
            AdOnboardingData(
                analytics = ItemNameIdAnalytics("", ""),
                image = "",
                accessibilityLabel = "",
                weight = 10,
                autoCloseTimeout = 10,
                null,
            )
        ),
        analytics = AdOnboardingPageData.Analytics(""),
    )

    init {
        Toolkit.setupTest()
    }

    @Test
    fun givenValidParams_resolveWithParams_returnsLayout() {
        mockkStatic("com.greencopper.core.data.KiboSerializableKt")
        every {
            any<Bundle>().putKiboSerializable(
                any(),
                any<MainActionCardData>()
            )
        } returns Bundle()
        mockBundleConstructor()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
        assertThat(initializer.resolveWithParams(validParams, "id")).isNotNull
    }

    @Test
    fun givenValidParamsNoImage_resolveWithParams_throws() {
        imageService.isImageAvailable_result = false

        assertThrows<ParameterizedOnboardingInitializerException.InvalidParametersException> {
            initializer.resolveWithParams(validParams, "id")
        }
    }

    @Test
    fun showInSequence_returnsFalse() {
        assertThat(initializer.showInSequence()).isFalse
    }
}
