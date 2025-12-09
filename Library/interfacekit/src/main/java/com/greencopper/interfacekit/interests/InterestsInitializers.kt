package com.greencopper.interfacekit.interests

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.interests.ui.InterestsFragment
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.onboarding.initializers.ParameterizedOnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal class InterestsPickerInitializer : ParameterizedFeatureInitializer<InterestsData>() {

    companion object {
        val key: FeatureKey = FeatureKey("InterfaceKit.InterestsPicker", 1)
    }

    override val featureKey: FeatureKey = key

    override fun layoutForParams(params: InterestsData): Layout = InterestsFragment(
        InterestsLayoutData(
            title = params.title,
            subtitle = params.subtitle,
            analytics = params.analytics,
            onboardingPageLayoutData = null,
        )
    )

    override fun decodeParams(params: FeatureParams): InterestsData = KiboSerializable.decodeFromJsonElement(params)

    override fun redirectionHashForParams(params: InterestsData): RedirectionHash =
        RedirectionHash(featureKey, params.analytics.screenName)
}

internal class InterestsOnboardingInitializer : ParameterizedOnboardingPageInitializer<InterestsData>() {

    companion object {
        val key = OnboardingPageKey("InterfaceKit.InterestsPicker", 1)
    }

    override fun resolveWithParams(params: InterestsData, pageId: String) = InterestsFragment(
        InterestsLayoutData(
            title = params.title,
            subtitle = params.subtitle,
            analytics = params.analytics,
            onboardingPageLayoutData = OnboardingPageLayoutData(pageId)
        )
    )

    override fun decodeParams(params: JsonElement): InterestsData = KiboSerializable.decodeFromJsonElement(params)

    override fun showInSequence(): Boolean = true
}

@Serializable
internal data class InterestsData(
    val title: String,
    val subtitle: String? = null,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<InterestsData> {

    override fun getSerializer(): KSerializer<InterestsData> = serializer()
}

@Serializable
internal data class InterestsLayoutData(
    val title: String,
    val subtitle: String?,
    val analytics: ScreenNameAnalytics,
    val onboardingPageLayoutData: OnboardingPageLayoutData?,
) : KiboSerializable<InterestsLayoutData> {
    override fun getSerializer(): KSerializer<InterestsLayoutData> = serializer()
}
