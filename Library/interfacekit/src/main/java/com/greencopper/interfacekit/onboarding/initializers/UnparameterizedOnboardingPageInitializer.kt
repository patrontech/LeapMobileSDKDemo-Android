package com.greencopper.interfacekit.onboarding.initializers

import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import kotlinx.serialization.json.JsonElement

public abstract class UnparameterizedOnboardingPageInitializer : OnboardingPageInitializer {
    override fun resolve(params: JsonElement?, pageId: String): OnboardingPageLayout {
        params?.let {
            throw IllegalArgumentException("params are not handled. Use Parameterized")
        }

        return resolve(pageId)
    }

    public abstract fun resolve(pageId: String): OnboardingPageLayout
}
