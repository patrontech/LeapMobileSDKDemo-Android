package com.greencopper.interfacekit.onboarding.pages

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class OnboardingPageKey(val name: String, val version: Int) :
    KiboSerializable<OnboardingPageKey> {
    override fun getSerializer(): KSerializer<OnboardingPageKey> = serializer()
}