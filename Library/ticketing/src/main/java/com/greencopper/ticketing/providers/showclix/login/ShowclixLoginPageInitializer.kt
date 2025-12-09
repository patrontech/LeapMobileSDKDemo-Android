package com.greencopper.ticketing.providers.showclix.login

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.onboarding.initializers.ParameterizedOnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.ticketing.providers.showclix.login.ui.ShowclixLoginFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal class ShowclixLoginPageInitializer :
    ParameterizedOnboardingPageInitializer<ShowclixLoginOnboardingData>() {

    override fun resolveWithParams(
        params: ShowclixLoginOnboardingData,
        pageId: String,
    ): OnboardingPageLayout {
        return ShowclixLoginFragment(params.toShowclixLoginOnboardingLayoutData(pageId))
    }

    override fun decodeParams(params: JsonElement): ShowclixLoginOnboardingData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun showInSequence(): Boolean = true

    companion object {
        val key = OnboardingPageKey("Ticketing.Showclix.Login", 1)
    }
}

@Serializable
internal data class ShowclixLoginOnboardingData(
    val apiUrl: String,
    val magicLink: String,
    val images: Images,
    val analytics: OnboardingPageLayoutData.OnboardingAnalytics? = null,
) : KiboSerializable<ShowclixLoginOnboardingData> {

    @Serializable
    data class Images(
        val enterEmail: String,
        val emailSent: String,
    )

    override fun getSerializer(): KSerializer<ShowclixLoginOnboardingData> = serializer()
}

@Serializable
internal data class ShowclixLoginOnboardingLayoutData(
    val apiUrl: String,
    val magicLink: String,
    val images: ShowclixLoginOnboardingData.Images,
    val onboardingPageLayoutData: OnboardingPageLayoutData,
) : KiboSerializable<ShowclixLoginOnboardingLayoutData> {

    override fun getSerializer(): KSerializer<ShowclixLoginOnboardingLayoutData> = serializer()
}

private fun ShowclixLoginOnboardingData.toShowclixLoginOnboardingLayoutData(pageId: String) =
    ShowclixLoginOnboardingLayoutData(
        apiUrl,
        magicLink,
        images,
        OnboardingPageLayoutData(
            pageId,
            analytics
        )
    )
