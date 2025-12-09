package com.greencopper.interfacekit.navigation.feature.info

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

internal class FeatureInfoTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()

    private val onboardingParam = "onboardingParam"
    private val onboardingValue = "onboardingValue"
    private val onboardingParams = JsonObject(
        mapOf(
            onboardingParam to json.encodeToJsonElement(onboardingValue)
        )
    )

    private val featureKey = FeatureKey("testKey", 1)
    private val featureParams = JsonObject(
        mapOf(
            "existingParam" to json.encodeToJsonElement("existingValue")
        )
    )
    private val featureInfo = FeatureInfo(featureKey, featureParams, onboardingParams)

    @Test
    fun withParams_changesParams_keepsKey_keepsOnboarding() {
        val newParam = "newParam"
        val newValue = "newValue"
        val newParams = JsonObject(
            mapOf(
                newParam to json.encodeToJsonElement(newValue)
            )
        )

        val newInfo = featureInfo.withParams(newParams)

        assertThat(newInfo.params).isEqualTo(newParams)
        assertThat(newInfo.key).isEqualTo(featureKey)
        assertThat(newInfo.onboarding).isEqualTo(onboardingParams)
    }
}