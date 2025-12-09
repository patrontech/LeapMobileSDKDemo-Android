package com.greencopper.interfacekit.onboarding

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class OnboardingContext(
    val redirectionHash: RedirectionHash? = null,
    val pages: List<OnboardingPageInfo>,
    val feature: FeatureInfo? = null,
    val isAppOnboarding: Boolean,
) : KiboSerializable<OnboardingContext> {
    override fun getSerializer(): KSerializer<OnboardingContext> = serializer()
}
