package com.greencopper.interfacekit.onboarding.pages

import com.greencopper.core.conditions.ConditionSet
import com.greencopper.core.conditions.Conditioned
import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class OnboardingPageInfo(
    val id: String,
    val key: OnboardingPageKey,
    val params: JsonElement? = null,
    override val conditionSet: ConditionSet? = null
) :
    KiboSerializable<OnboardingPageInfo>, Conditioned {
    override fun getSerializer(): KSerializer<OnboardingPageInfo> = serializer()
}