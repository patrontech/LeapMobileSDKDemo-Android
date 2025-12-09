package com.greencopper.interfacekit.onboarding

import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout

public interface OnboardingController {

    public val onboardingSequence: OnboardingSequence

    public fun getLayoutToDisplay(): Layout?

    public fun pageDidComplete(
        onboardingContainerLayout: OnboardingContainerLayout,
        pageId: String,
        persistAsCompleted: Boolean
    )

    public fun closeOnboarding(onboardingContainerLayout: OnboardingContainerLayout)
}

public data class OnboardingSequence(
    var pages: List<OnboardingPageInfo>,
) {
    public fun toViewData(onboardingPageId: String): OnboardingSequenceViewData = OnboardingSequenceViewData(
        numberOfPages = pages.size,
        selectedPage = pages.indexOfFirst { it.id == onboardingPageId },
    )
}

public data class OnboardingSequenceViewData(
    val numberOfPages: Int,
    val selectedPage: Int,
)
