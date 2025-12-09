package com.greencopper.interfacekit.onboarding.maincard

import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MainActionCardDataTest {

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
    }

    @Test
    fun serialize_deserialize() {
        val data = MainActionCardData(
            title = "title",
            text = "text",
            backgroundImage = "backgroundImage",
            analytics = OnboardingPageLayoutData.OnboardingAnalytics(
                screenName = "screenName",
                featureName = "featureName"
            ),
            mainButton = MainActionCardDataActionButton(
                title = "title",
                action = MainActionCardDataActionButton.Action(
                    type = "type",
                    request = "request",
                    analyticsEvent = "event"
                )
            ),
            skipButton = MainActionCardDataActionButton(
                title = "title",
                action = MainActionCardDataActionButton.Action(
                    type = "type",
                    analyticsEvent = "event"
                )
            )
        )
        testKiboSerializable(data)
    }

    @Test
    fun serialize_deserializeLayout() {
        val data = MainActionCardLayoutData(
            title = "title",
            text = "text",
            backgroundImage = "backgroundImage",
            mainButton = MainActionCardDataActionButton(
                title = "title",
                action = MainActionCardDataActionButton.Action(
                    type = "type",
                    request = "request",
                    analyticsEvent = "event"
                )
            ),
            skipButton = MainActionCardDataActionButton(
                title = "title",
                action = MainActionCardDataActionButton.Action(
                    type = "type",
                    analyticsEvent = "event"
                )
            ),
            onboardingPageLayoutData = OnboardingPageLayoutData(
                "pageId",
                OnboardingPageLayoutData.OnboardingAnalytics(
                    screenName = "screenName",
                    featureName = "featureName"
                )
            )
        )
        testKiboSerializable(data)
    }
}
