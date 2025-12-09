package com.greencopper.thuzi.account.registration.initializer

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.*
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.onboarding.initializers.OnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.ThuziRegisteredCondition
import com.greencopper.thuzi.account.registration.model.RegistrationLayoutData
import com.greencopper.thuzi.account.registration.recipe.RegistrationConfigurationHolder
import com.greencopper.thuzi.account.registration.ui.RegistrationFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal class RegistrationInitializer(
    private val localStorage: LocalStorage,
    private val registrationConfigurationHolder: RegistrationConfigurationHolder,
) : ParameterizedFeatureInitializer<RegistrationData>(), OnboardingPageInitializer {

    companion object {
        val key: FeatureKey = FeatureKey("Thuzi.Registration", 1)
    }

    override val featureKey: FeatureKey = key

    private val registeredCondition: ThuziRegisteredCondition =
        ThuziRegisteredCondition(localStorage)

    override fun decodeParams(params: FeatureParams): RegistrationData =
        KiboSerializable.decodeFromJsonElement(params)

    private fun createLayout(params: RegistrationData, pageId: String = "NotAnOnboardingPage"): RegistrationFragment {
        val registered = ThuziRegisteredCondition.ThuziRegisteredConditionData(true)
        if (registeredCondition.checkWith(registered)) {
            throw AlreadyRegisteredException()
        }

        val config = registrationConfigurationHolder.currentConfiguration.value
            ?: throw FeatureInitializerException.NoParametersProvidedException()

        localStorage.project.thuzi.config.value = config

        val deviceLinkingUrl = config.deviceLinkingUrl?.let {
            localStorage.replaceUrlParameters(it)
        }
        val activationUrl = config.activationUrl.let {
            localStorage.replaceUrlParameters(it)
        }

        val registrationUrl = params.registrationUrl
            ?: deviceLinkingUrl
            ?: activationUrl

        return RegistrationFragment(
            RegistrationLayoutData(
                registrationUrl = registrationUrl,
                activationUrl = activationUrl,
                config = config,
                onSuccessFeatureInfo = params.onSuccessFeatureInfo,
                onboardingAnalytics = params.analytics,
                pageId = pageId,
                redirectionHash = redirectionHashForParams(params)
            )
        )
    }

    override fun layoutForParams(params: RegistrationData): Layout = createLayout(params)

    override fun redirectionHashForParams(params: RegistrationData): RedirectionHash {
        return if (params.redirectionHash != null) {
            params.redirectionHash
        } else {
            val analytics = registrationConfigurationHolder.currentConfiguration.value?.analytics
                ?: throw FeatureInitializerException.NoParametersProvidedException()
            RedirectionHash(key, analytics.screenName)
        }
    }

    override fun resolve(params: JsonElement?, pageId: String): OnboardingPageLayout {
        val data = params?.let { decodeParams(it) }
            ?: throw IllegalStateException("No parameters provided for ProjectSwitcher onboarding screen")

        return createLayout(data, pageId)
    }

    override fun showInSequence(): Boolean = true
}

@Serializable
internal data class RegistrationData(
    val onSuccessFeatureInfo: FeatureInfo? = null,
    val registrationUrl: String? = null,
    val analytics: OnboardingPageLayoutData.OnboardingAnalytics? = null,
    val redirectionHash: RedirectionHash? = null,
) : KiboSerializable<RegistrationData> {

    override fun getSerializer(): KSerializer<RegistrationData> = serializer()
}

internal class AlreadyRegisteredException : Exception("User is already registered")
