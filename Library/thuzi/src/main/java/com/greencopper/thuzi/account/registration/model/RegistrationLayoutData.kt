package com.greencopper.thuzi.account.registration.model

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.interfacekit.webview.data.WebViewBaseData
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class RegistrationLayoutData(
    override val url: String,
    val activationUrl: String,
    val apiUrl: String,
    val brandId: String,
    val eventId: String,
    val onSuccessFeatureInfo: FeatureInfo?,
    val analytics: ScreenNameAnalytics,
    val onboardingPageLayoutData: OnboardingPageLayoutData?,
    val redirectionHash: RedirectionHash,
) : WebViewBaseData<RegistrationLayoutData>, KiboSerializable<RegistrationLayoutData> {

    public constructor(
        registrationUrl: String,
        activationUrl: String,
        config: RegistrationConfiguration,
        onSuccessFeatureInfo: FeatureInfo?,
        onboardingAnalytics: OnboardingPageLayoutData.OnboardingAnalytics?,
        pageId: String,
        redirectionHash: RedirectionHash,
    ) : this(
        url = registrationUrl,
        activationUrl = activationUrl,
        apiUrl = config.apiUrl,
        brandId = config.brandId,
        eventId = config.eventId,
        onSuccessFeatureInfo = onSuccessFeatureInfo,
        analytics = config.analytics,
        onboardingPageLayoutData = OnboardingPageLayoutData(pageId, onboardingAnalytics),
        redirectionHash = redirectionHash
    )

    override fun getSerializer(): KSerializer<RegistrationLayoutData> = serializer()

    internal fun getFeatureParamsAnalytics(): ScreenNameAnalytics? {
        return onSuccessFeatureInfo?.params?.let { params ->
            try {
                KiboSerializable.decodeFromJsonElement<Params>(params).analytics
            } catch (throwable: Throwable) {
                App.log.e(
                    message = "Error parsing FeatureParamsAnalytics $analytics",
                    throwable = throwable
                )
                null
            }
        }
    }

    @Serializable
    internal data class Params(val analytics: ScreenNameAnalytics)
}
