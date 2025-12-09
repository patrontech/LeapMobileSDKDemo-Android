package com.greencopper.interfacekit.navigation.feature

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.mocks.TestParameter
import com.greencopper.interfacekit.mocks.TestParameterizedFeatureFragmentInitializer
import com.greencopper.interfacekit.mocks.TestParameterizedFragment
import com.greencopper.interfacekit.mocks.TestParams
import com.greencopper.interfacekit.mocks.TestUnparameterizedFeatureFragmentInitializer
import com.greencopper.interfacekit.mocks.TestUnparameterizedFragment
import com.greencopper.interfacekit.navigation.feature.DIFeatureResolver.OnboardingParams
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.onboarding.maincard.MainActionCardPageInitializer
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.testmocks.mockBundleConstructor
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DIFeatureResolverTest {

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
        App.resolve<Registrar>().apply {
            bindFeature(TestUnparameterizedFeatureFragmentInitializer.key) {
                TestUnparameterizedFeatureFragmentInitializer()
            }
            bindFeature(TestParameterizedFeatureFragmentInitializer.key) {
                TestParameterizedFeatureFragmentInitializer()
            }
        }

        mockBundleConstructor()
    }

    private val featureResolver = DIFeatureResolver()
    private val json: Json = App.resolve()

    @Test
    fun resolve_unparameterized_fragment_with_params() {
        val boundFeatureKey = TestUnparameterizedFeatureFragmentInitializer.key
        val boundFeatureParams = TestParams("testKey" to "testValue")
        val boundFeatureInfo =
            FeatureInfo(
                boundFeatureKey,
                boundFeatureParams.toJsonObject()
            )
        val layout = featureResolver.resolve(boundFeatureInfo)
        assertThat(layout).isInstanceOf(TestUnparameterizedFragment::class.java)
    }

    @Test
    fun resolve_unparameterized_fragment_without_params() {
        val boundFeatureKey = TestUnparameterizedFeatureFragmentInitializer.key
        val boundFeatureInfo =
            FeatureInfo(
                boundFeatureKey,
                JsonNull
            )
        val layout = featureResolver.resolve(boundFeatureInfo)
        assertThat(layout).isInstanceOf(TestUnparameterizedFragment::class.java)
    }

    @Test
    fun resolve_parameterized_fragment_with_right_parameters() {
        val boundFeatureKey = TestParameterizedFeatureFragmentInitializer.key
        val boundFeatureParams = TestParameter("my_title", 1)
        val boundFeatureInfo =
            FeatureInfo(
                boundFeatureKey,
                boundFeatureParams.encodeToJsonElement()
            )
        val layout = featureResolver.resolve(boundFeatureInfo)
        assertThat(layout).isInstanceOf(TestParameterizedFragment::class.java)
    }

    @Test
    fun resolve_parameterized_fragment_with_onboarding_parameters() {
        val onboardingParams = OnboardingParams(listOf(
            OnboardingPageInfo(
                id = "notification",
                key = OnboardingPageKey("InterfaceKit.OnboardingPage.MainActionCard", 1),
            )
        ))
        val boundFeatureKey = TestParameterizedFeatureFragmentInitializer.key
        val boundFeatureParams = TestParameter("my_title", 1)
        val boundFeatureInfo =
            FeatureInfo(
                boundFeatureKey,
                boundFeatureParams.encodeToJsonElement(),
                json.encodeToJsonElement(onboardingParams),
            )
        val layout = featureResolver.resolve(boundFeatureInfo)
        assertThat(layout).isInstanceOf(OnboardingContainerLayout::class.java)
    }

    @Test
    fun resolve_parameterized_fragment_with_wrong_onboarding_parameters() {
        val onboardingParams = OnboardingParams(emptyList())
        val boundFeatureKey = TestParameterizedFeatureFragmentInitializer.key
        val boundFeatureParams = TestParameter("my_title", 1)
        val boundFeatureInfo =
            FeatureInfo(
                boundFeatureKey.copy(name = "nonsense"),
                boundFeatureParams.encodeToJsonElement(),
                json.encodeToJsonElement(onboardingParams),
            )
        assertThrows<FeatureResolverException.FeatureNotRegisteredException> {
            (featureResolver.resolve(boundFeatureInfo))
        }
    }

    @Test
    fun resolve_parameterized_fragment_with_null_parameters() {
        val boundFeatureKey = TestParameterizedFeatureFragmentInitializer.key
        val boundFeatureInfo =
            FeatureInfo(
                boundFeatureKey,
                null
            )
        val exception = assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            featureResolver.resolve(boundFeatureInfo)
        }
        assertThat(exception)
            .hasMessage("[FeatureInitializerException] Couldn't retrieve Layout, parameters were required but not provided.")
    }

    @Serializable
    private data class WrongParameter(val random: String) : KiboSerializable<WrongParameter> {
        override fun getSerializer(): KSerializer<WrongParameter> = serializer()
    }

    @Test
    fun resolve_parameterized_fragment_with_wrong_parameters() {
        val boundFeatureKey = TestParameterizedFeatureFragmentInitializer.key
        val wrongParameter = WrongParameter("")
        val params = wrongParameter.encodeToJsonElement()
        val boundFeatureInfo =
            FeatureInfo(
                boundFeatureKey,
                params
            )
        val exception = assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            featureResolver.resolve(boundFeatureInfo)
        }
        assertThat(exception)
            .hasMessage("[FeatureInitializerException] Couldn't decode parameters $params")
    }

    @Test
    fun resolve_unknown() {
        val featureKey = FeatureKey("Test.ResolverUnknown", 1)
        val featureParams = null
        val featureInfo = FeatureInfo(featureKey, featureParams)
        val exception = assertThrows<FeatureResolverException.FeatureNotRegisteredException> {
            featureResolver.resolve(featureInfo)
        }
        assertThat(exception)
            .hasMessage("[FeatureResolverException] Couldn't find Initializer associated with key $featureKey.")
        assertThat(exception.key).isEqualTo(featureKey)
    }

    @Test
    fun serialize() {
        val pageInfo = OnboardingPageInfo("id", MainActionCardPageInitializer.key)
        val data = OnboardingParams(listOf(pageInfo))
        val encoded = data.encodeToString()
        val decoded = KiboSerializable.decodeFromString<OnboardingParams>(encoded)
        assertThat(decoded).isEqualTo(data)
    }
}
