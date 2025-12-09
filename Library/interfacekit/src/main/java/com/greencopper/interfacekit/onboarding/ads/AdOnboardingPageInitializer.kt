package com.greencopper.interfacekit.onboarding.ads

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ItemNameIdAnalytics
import com.greencopper.interfacekit.imageservice.ImageService
import com.greencopper.interfacekit.onboarding.ads.ui.AdOnboardingFragment
import com.greencopper.interfacekit.onboarding.initializers.ParameterizedOnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.interfacekit.utils.Weighted
import com.greencopper.interfacekit.utils.randomByWeight
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal class AdOnboardingPageInitializer(
    private val imageService: ImageService,
) : ParameterizedOnboardingPageInitializer<AdOnboardingPageData>() {

    companion object {
        val key = OnboardingPageKey("InterfaceKit.OnboardingPage.Ad", 1)
    }

    override fun resolveWithParams(params: AdOnboardingPageData, pageId: String): OnboardingPageLayout {
        val ad = getValidAd(params.ads) ?: throw ParameterizedOnboardingInitializerException.InvalidParametersException()

        return AdOnboardingFragment(
            AdOnboardingLayoutData(
                analytics = AdOnboardingLayoutData.Analytics(
                    itemName = ad.analytics.itemName,
                    itemId = ad.analytics.itemId,
                    featureName = params.analytics?.featureName,
                ),
                image = ad.image,
                accessibilityLabel = ad.accessibilityLabel,
                autoCloseTimeout = ad.autoCloseTimeout,
                onTap = ad.onTap,
                onboardingPageLayoutData = OnboardingPageLayoutData(pageId)
            )
        )
    }

    override fun decodeParams(params: JsonElement): AdOnboardingPageData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun showInSequence(): Boolean = false

    private fun getValidAd(ads: List<AdOnboardingData>): AdOnboardingData? {
        if (ads.isEmpty()) return null

        val randomAd = randomByWeight(ads) as AdOnboardingData

        return if (imageService.isImageAvailable(randomAd.image)) {
            randomAd
        } else {
            getValidAd(ads.minus(randomAd))
        }
    }
}

@Serializable
internal data class AdOnboardingPageData(
    val ads: List<AdOnboardingData>,
    val analytics: Analytics? = null,
) : KiboSerializable<AdOnboardingPageData> {
    override fun getSerializer(): KSerializer<AdOnboardingPageData> = serializer()

    @Serializable
    internal data class Analytics(
        val featureName: String? = null,
    )
}

@Serializable
internal data class AdOnboardingData(
    val analytics: ItemNameIdAnalytics,
    val image: String,
    val accessibilityLabel: String,
    override val weight: Int,
    val autoCloseTimeout: Int, // seconds
    val onTap: String? = null, // RouteLink
) : Weighted

@Serializable
internal data class AdOnboardingLayoutData(
    val analytics: Analytics,
    val image: String,
    val accessibilityLabel: String,
    val autoCloseTimeout: Int, // seconds
    val onTap: String?, // RouteLink
    val onboardingPageLayoutData: OnboardingPageLayoutData,
) : KiboSerializable<AdOnboardingLayoutData> {
    override fun getSerializer(): KSerializer<AdOnboardingLayoutData> = serializer()

    @Serializable
    internal data class Analytics(
        val itemName: String,
        val itemId: String,
        val featureName: String?,
    )
}
