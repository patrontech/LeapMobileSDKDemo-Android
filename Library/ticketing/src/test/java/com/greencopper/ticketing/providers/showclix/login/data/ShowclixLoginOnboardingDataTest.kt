package com.greencopper.ticketing.providers.showclix.login.data

import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.ticketing.providers.showclix.login.ShowclixLoginOnboardingData
import com.greencopper.ticketing.providers.showclix.login.ShowclixLoginOnboardingLayoutData
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class ShowclixLoginOnboardingDataTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun serializeAndDeserialize() {
        val originalData = ShowclixLoginOnboardingData(
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

        assertDoesNotThrow {
            testKiboSerializable(originalData)
        }
    }

    @Test
    fun serializeAndDeserializeLayoutData() {
        val originalData = ShowclixLoginOnboardingLayoutData(
            "apiUrl",
            "magicLink",
            ShowclixLoginOnboardingData.Images(
                "enterEmail",
                "emailSent"
            ),
            OnboardingPageLayoutData(
                "pageId",
                OnboardingPageLayoutData.OnboardingAnalytics(
                    "screenName",
                    "featureName"
                ),
            )
        )

        assertDoesNotThrow {
            testKiboSerializable(originalData)
        }
    }
}
