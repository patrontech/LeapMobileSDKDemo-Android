package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.onboarding.OnboardingController
import com.greencopper.interfacekit.onboarding.OnboardingSequence
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout

public class MockOnboardingController(
    override val onboardingSequence: OnboardingSequence = OnboardingSequence(listOf()),
    private val setupLayoutValue: () -> Layout? = { null },
    private val pageDidCompleteValue: (OnboardingContainerLayout, String, Boolean) -> Unit = { _, _, _ -> }
) : OnboardingController {
    override fun getLayoutToDisplay(): Layout? = setupLayoutValue()

    override fun pageDidComplete(
        onboardingContainerLayout: OnboardingContainerLayout,
        pageId: String,
        persistAsCompleted: Boolean
    ): Unit = pageDidCompleteValue(onboardingContainerLayout, pageId, persistAsCompleted)

    override fun closeOnboarding(onboardingContainerLayout: OnboardingContainerLayout) {}
}
