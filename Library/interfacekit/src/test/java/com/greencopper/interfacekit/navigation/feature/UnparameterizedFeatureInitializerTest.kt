package com.greencopper.interfacekit.navigation.feature

import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.Layout
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UnparameterizedFeatureInitializerTest {

    private val initializer = MockUnparameterizedFeatureInitializer()

    @Test
    fun whenParamsAreNull_getLayout_returnsLayout() {
        val layout = initializer.getLayout(null)
        assertThat(layout).isEqualTo(initializer._layout)
    }

    @Test
    fun whenParamsInvalid_getLayout_returnsLayout() {
        val layout = initializer.getLayout(JsonNull)
        assertThat(layout).isEqualTo(initializer._layout)
    }

    @Test
    fun whenParamsAreNull_redirectionHash_returnsDefault() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash.featureKey).isEqualTo(initializer.featureKey)
    }

    @Test
    fun whenParamsInvalid_redirectionHash_returnsDefault() {
        val redirectionHash = initializer.redirectionHashFor(JsonNull)
        assertThat(redirectionHash.featureKey).isEqualTo(initializer.featureKey)
    }
}

private class MockUnparameterizedFeatureInitializer : UnparameterizedFeatureInitializer() {

    val _layout = Layout()

    override val featureKey: FeatureKey = FeatureKey("FeatureInitializer.Test", 1)

    override fun getLayout(): Layout = _layout
}