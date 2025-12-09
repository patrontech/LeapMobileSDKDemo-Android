package com.greencopper.interfacekit.navigation.feature

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.onboarding.OnboardingContext
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.binding.Creator
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.container.Key
import com.greencopper.toolkit.di.resolver.tryResolve
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class DIFeatureResolver : FeatureResolver {
    override fun resolve(info: FeatureInfo): Layout {
        info.onboarding?.let {
            val redirectionHash = resolveInitializer(info).redirectionHashFor(info.params)
            val argument = OnboardingContext(
                redirectionHash = redirectionHash,
                pages = KiboSerializable.decodeFromJsonElement<OnboardingParams>(it).pages,
                feature = info.copy(onboarding = null),
                isAppOnboarding = false,
            )

            return OnboardingContainerLayout.newInstance(argument)
        }
        return resolveInitializer(info).getLayout(info.params)
    }

    override fun resolveInitializer(info: FeatureInfo): FeatureInitializer {
        return App.tryResolve(tag = info.key)
            ?: throw FeatureResolverException.FeatureNotRegisteredException(info.key)
    }

    @Serializable
    internal data class OnboardingParams(
        val pages: List<OnboardingPageInfo>
    ) : KiboSerializable<OnboardingParams> {
        override fun getSerializer(): KSerializer<OnboardingParams> = serializer()
    }
}

public fun Registrar.bindFeature(
    featureKey: FeatureKey,
    featureInitializer: Creator<FeatureInitializer>
): Key = bindProvider(tag = featureKey, featureInitializer)
