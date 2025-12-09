package com.greencopper.interfacekit.onboarding.maincard

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.onboarding.initializers.ParameterizedOnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.maincard.ui.MainActionCardFragment
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal class MainActionCardPageInitializer : ParameterizedOnboardingPageInitializer<MainActionCardData>() {

    override fun resolveWithParams(
        params: MainActionCardData,
        pageId: String,
    ): OnboardingPageLayout {
        return MainActionCardFragment(params.toMainActionCardLayoutData(pageId))
    }

    override fun decodeParams(params: JsonElement): MainActionCardData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun showInSequence(): Boolean = true

    companion object {
        val key = OnboardingPageKey("InterfaceKit.OnboardingPage.MainActionCard", 1)
    }
}

@Serializable
public data class MainActionCardData(
    val title: String,
    val text: String,
    val backgroundImage: String,
    val analytics: OnboardingPageLayoutData.OnboardingAnalytics,
    val mainButton: MainActionCardDataActionButton? = null,
    val skipButton: MainActionCardDataActionButton? = null,
) : KiboSerializable<MainActionCardData> {

    override fun getSerializer(): KSerializer<MainActionCardData> = serializer()
}

@Serializable
public data class MainActionCardLayoutData(
    val title: String,
    val text: String,
    val backgroundImage: String,
    val mainButton: MainActionCardDataActionButton? = null,
    val skipButton: MainActionCardDataActionButton? = null,
    val onboardingPageLayoutData: OnboardingPageLayoutData,
) : KiboSerializable<MainActionCardLayoutData> {

    override fun getSerializer(): KSerializer<MainActionCardLayoutData> = serializer()
}

private fun MainActionCardData.toMainActionCardLayoutData(pageId: String) =
    MainActionCardLayoutData(
        title = title,
        text = text,
        backgroundImage = backgroundImage,
        mainButton = mainButton,
        skipButton = skipButton,
        onboardingPageLayoutData = OnboardingPageLayoutData(
            pageId,
            analytics
        )
    )
