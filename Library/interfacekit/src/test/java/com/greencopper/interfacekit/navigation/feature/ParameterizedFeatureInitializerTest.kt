package com.greencopper.interfacekit.navigation.feature

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ParameterizedFeatureInitializerTest {

    private val initializer = MockParameterizedFeatureInitializer()

    init {
        Toolkit.setupTest()
    }

    @Test
    fun whenParamsAreNull_getLayout_throwsNoParametersProvidedException() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenParamsInvalid_getLayout_throwsParametersDecodeFailedException() {
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(JsonNull)
        }
    }

    @Test
    fun whenParamsValid_getLayout_returnsLayout() {
        val layout = initializer.getLayout(JsonPrimitive("test"))
        assertThat(layout).isEqualTo(initializer.layout)
    }

    @Test
    fun whenParamsAreNull_redirectionHash_returnsDefault() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash.featureKey).isEqualTo(initializer.featureKey)
        assertThat(redirectionHash).isNotEqualTo(initializer.redirectionHash)
    }

    @Test
    fun whenParamsInvalid_redirectionHash_returnsDefault() {
        val redirectionHash = initializer.redirectionHashFor(JsonNull)
        assertThat(redirectionHash.featureKey).isEqualTo(initializer.featureKey)
        assertThat(redirectionHash).isNotEqualTo(initializer.redirectionHash)
    }

    @Test
    fun whenParamsValid_redirectionHash_returnsRedirectionHash() {
        val redirectionHash = initializer.redirectionHashFor(JsonPrimitive("test'"))
        assertThat(redirectionHash).isEqualTo(initializer.redirectionHash)
    }
}

private class MockParameterizedFeatureInitializer : ParameterizedFeatureInitializer<String>() {

    val redirectionHash: RedirectionHash
        get() = RedirectionHash(featureKey, "identifier")

    val layout = Layout()

    override val featureKey: FeatureKey = FeatureKey("FeatureInitializer.Test", 1)

    override fun decodeParams(params: FeatureParams): String = KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: String): Layout = layout

    override fun redirectionHashForParams(params: String): RedirectionHash = redirectionHash
}