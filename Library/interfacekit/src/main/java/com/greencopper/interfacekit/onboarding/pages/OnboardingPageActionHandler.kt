package com.greencopper.interfacekit.onboarding.pages

import com.greencopper.interfacekit.navigation.layout.Layout

public interface OnboardingPageActionHandler {
    public suspend fun executeAction(action: OnboardingPageAction, origin: Layout): Boolean
}