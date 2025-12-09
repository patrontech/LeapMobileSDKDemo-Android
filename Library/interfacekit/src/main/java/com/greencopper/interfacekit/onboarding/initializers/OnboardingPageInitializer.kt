package com.greencopper.interfacekit.onboarding.initializers

import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import kotlinx.serialization.json.JsonElement

public interface OnboardingPageInitializer {
    public fun resolve(params: JsonElement?, pageId: String): OnboardingPageLayout
    /** Return true if the page being initialized should be displayed to the user as part of the onboarding sequence */
    public fun showInSequence(): Boolean
}
