package com.greencopper.interfacekit.onboarding.recipe

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class OnboardingConfiguration(
    val pages: List<OnboardingPageInfo>,
) : KiboSerializable<OnboardingConfiguration> {
    override fun getSerializer(): KSerializer<OnboardingConfiguration> = serializer()
}
