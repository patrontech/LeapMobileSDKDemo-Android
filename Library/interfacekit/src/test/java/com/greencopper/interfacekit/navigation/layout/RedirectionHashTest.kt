package com.greencopper.interfacekit.navigation.layout

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.testmocks.mockAppResolve
import com.greencopper.toolkit.App
import com.greencopper.toolkit.serialization.JsonFactory
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RedirectionHashTest {
    @BeforeEach
    internal fun setUp() {
        App = mockk()
        mockAppResolve(JsonFactory.create())
    }

    @Test
    fun deserialize() {
        val jsonString =
            "{\"featureKey\":{\"name\":\"feature\",\"version\":1},\"identifier\":\"hello\"}"
        val jsonObject = KiboSerializable.decodeFromString<RedirectionHash>(jsonString)
        Assertions.assertThat(jsonObject.featureKey.name).isEqualTo("feature")
        Assertions.assertThat(jsonObject.featureKey.version).isEqualTo(1)
        Assertions.assertThat(jsonObject.identifier).isEqualTo("hello")
    }

    @Test
    fun serialize() {
        val data = RedirectionHash(
            featureKey = FeatureKey(
                name = "feature",
                version = 1
            ),
            identifier = "identifier"
        )
        val encoded = data.encodeToString()
        val decoded = KiboSerializable.decodeFromString<RedirectionHash>(encoded)
        Assertions.assertThat(decoded).isEqualTo(data)
    }
}