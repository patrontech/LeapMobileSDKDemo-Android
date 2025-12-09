package com.greencopper.interfacekit.onboarding.initializers

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.onboarding.OnboardingContext
import com.greencopper.interfacekit.onboarding.OnboardingController
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.di.resolver.tryResolve
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public class OnboardingFeatureInitializer(private val resolver: Resolver)
    : ParameterizedFeatureInitializer<OnboardingFeatureData>() {

    public companion object {
        public val key: FeatureKey = FeatureKey("InterfaceKit.Onboarding", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): OnboardingFeatureData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: OnboardingFeatureData): Layout {
        val redirectionHash = redirectionHashForParams(params)

        val context = OnboardingContext(redirectionHash, params.pages, null, false)
        val onboardingController = resolver.tryResolve<OnboardingController>(context)

        return onboardingController?.getLayoutToDisplay()
            ?: throw FeatureInitializerException.ParametersNotValid(params.encodeToJsonElement())
    }

    override fun redirectionHashForParams(params: OnboardingFeatureData): RedirectionHash =
        RedirectionHash(key, params.redirectionId)
}

@Serializable
public data class OnboardingFeatureData(
    val pages: List<OnboardingPageInfo>,
    val redirectionId: String
) : KiboSerializable<OnboardingFeatureData> {
    override fun getSerializer(): KSerializer<OnboardingFeatureData> = serializer()
}
