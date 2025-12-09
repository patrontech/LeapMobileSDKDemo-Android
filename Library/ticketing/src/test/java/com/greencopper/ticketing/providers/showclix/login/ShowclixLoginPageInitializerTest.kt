package com.greencopper.ticketing.providers.showclix.login

import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class ShowclixLoginPageInitializerTest {

    private val initializer: ShowclixLoginPageInitializer

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())

        initializer = ShowclixLoginPageInitializer()
    }

    private val originalData = ShowclixLoginOnboardingData(
        "apiUrl",
        "magicLink",
        ShowclixLoginOnboardingData.Images(
            "enterEmail",
            "emailSent"
        ),
        OnboardingPageLayoutData.OnboardingAnalytics(
            "screenName",
            "featureName"
        )
    )

    @Test
    fun verifyKey() {
        assertThat(ShowclixLoginPageInitializer.key).isEqualTo(
            OnboardingPageKey(
                "Ticketing.Showclix.Login",
                1
            )
        )
    }

    @Test
    fun resolveWithParams_shouldReturnPage() {
        mockBundleConstructor()

        assertDoesNotThrow {
            initializer.resolve(originalData.encodeToJsonElement(), "id")
        }
    }

    @Test
    fun showInSequence_returnsTrue() {
        assertThat(initializer.showInSequence()).isTrue
    }
}
