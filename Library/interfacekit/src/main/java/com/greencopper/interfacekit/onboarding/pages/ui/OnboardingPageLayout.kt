package com.greencopper.interfacekit.onboarding.pages.ui

import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.onboarding.OnboardingController
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import kotlinx.serialization.Serializable

@Serializable
public data class OnboardingPageLayoutData(
    val pageId: String,
    val onboardingAnalytics: OnboardingAnalytics? = null,
) {
    @Serializable
    public data class OnboardingAnalytics(val screenName: String, val featureName: String? = null)
}

public interface OnboardingPageLayout {
    public val onboardingScreenViewEvent: ScreenViewEvent?
    public val onboardingPageId: String

    public val ParameterizedFragment<*>.onboardingPageDelegate: Delegate?
        get() = parentFragment as? Delegate

    public val layout: Layout
        get() = this as Layout

    public fun createPageIdMissingException(): NoSuchElementException =
        NoSuchElementException("PageId isn't available for ${this::class.java.simpleName}")

    public interface Delegate {
        public val onboardingController: OnboardingController
        public fun pageDidComplete(pageId: String, persistAsCompleted: Boolean)
    }
}
