package com.greencopper.interfacekit.onboarding.initializers

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.onboarding.initializers.ParameterizedOnboardingPageInitializer.ParameterizedOnboardingInitializerException.DecodingFailedException
import com.greencopper.interfacekit.onboarding.initializers.ParameterizedOnboardingPageInitializer.ParameterizedOnboardingInitializerException.NoParametersProvidedError
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement

public abstract class ParameterizedOnboardingPageInitializer<T : KiboSerializable<T>> :
    OnboardingPageInitializer {
    override fun resolve(params: JsonElement?, pageId: String): OnboardingPageLayout {
        params ?: throw NoParametersProvidedError()

        try {
            return resolveWithParams(decodeParams(params), pageId)
        } catch (decodingException: SerializationException) {
            throw DecodingFailedException(params)
        }
    }

    public abstract fun resolveWithParams(params: T, pageId: String): OnboardingPageLayout

    protected abstract fun decodeParams(params: JsonElement): T

    public sealed class ParameterizedOnboardingInitializerException : Throwable() {
        public class NoParametersProvidedError : ParameterizedOnboardingInitializerException() {
            override val message: String
                get() = "[${this::class.java.superclass?.simpleName}] Couldn't retrieve Layout, parameters were required but not provided."
        }

        public class InvalidParametersException(private val params: FeatureParams? = null) :
            ParameterizedOnboardingInitializerException() {
            override val message: String
                get() = "[${this::class.java.superclass?.simpleName}] Provided parameters doesn't meet the requirements to show this layout : $params."
        }

        public class DecodingFailedException(private val params: FeatureParams? = null) :
            ParameterizedOnboardingInitializerException() {
            override val message: String
                get() = "[${this::class.java.superclass?.simpleName}] Decoding parameters failed : $params."
        }
    }
}
