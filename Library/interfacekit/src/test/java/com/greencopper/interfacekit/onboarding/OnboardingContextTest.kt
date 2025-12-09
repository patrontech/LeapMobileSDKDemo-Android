package com.greencopper.interfacekit.onboarding

import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testSerializable
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Test

internal class OnboardingContextTest {

    @Test
    fun serialize() {
        Toolkit.setupTest()
        val context = OnboardingContext(
            redirectionHash = RedirectionHash(
                FeatureKey("feature", 1)
            ),
            pages = listOf(
                OnboardingPageInfo(
                    id = "page_1",
                    key = OnboardingPageKey(
                        name = "key",
                        1
                    )
                )
            ),
            isAppOnboarding = false,
        )

        testSerializable(context)
    }
}
