package com.greencopper.interfacekit.navigation

import com.greencopper.interfacekit.mocks.TestParams
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EmbeddedInfoTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()

    @Test
    fun accessingAttributes() {
        val key =
            FeatureKey(
                "Test.Embedded",
                1
            )
        val params = TestParams("instance" to "24")
        val featureInfo =
            FeatureInfo(
                key,
                params.toJsonObject()
            )
        assertThat(featureInfo.key.name).isEqualTo("Test.Embedded")
        assertThat(featureInfo.key.version).isEqualTo(1)
        assertThat(
            featureInfo.params?.toDataModel<TestParams>()?.pairs?.get("instance")
        ).isEqualTo("24")
    }

    @Test
    fun serialization_fromString() {
        val key =
            FeatureKey(
                "Test.Embedded",
                1
            )
        val params =
            JsonObject(mapOf("age" to JsonPrimitive("24"), "coolLayout" to JsonPrimitive("true")))
        val featureInfo =
            FeatureInfo(
                key,
                params
            )
        val configString =
            """{"key":{"name":"Test.Embedded","version":1},"params":{"age":"24","coolLayout":"true"}}"""
        val serializedConfiguration =
            json.decodeFromString(FeatureInfo.serializer(), configString)
        assertThat(serializedConfiguration).isEqualTo(featureInfo)
    }

    @Test
    fun serializationWithEmptyParams() {
        val key =
            FeatureKey(
                "Test.Embedded",
                1
            )
        val params = null
        val featureInfo =
            FeatureInfo(
                key,
                params
            )
        val configString =
            json.encodeToString(FeatureInfo.serializer(), featureInfo)
        val serializedConfiguration =
            json.decodeFromString(FeatureInfo.serializer(), configString)
        assertThat(serializedConfiguration).isEqualTo(featureInfo)
    }

    @Test
    fun serializationWithNullParams() {
        val key =
            FeatureKey(
                "Test.Embedded",
                1
            )
        val params = null
        val featureInfo =
            FeatureInfo(
                key,
                params
            )
        val configString = """{"key":{"name":"Test.Embedded","version":1},"params":null}"""
        val serializedConfiguration =
            json.decodeFromString(FeatureInfo.serializer(), configString)
        assertThat(serializedConfiguration).isEqualTo(featureInfo)
    }

    @Test
    fun serializationWithNoParams() {
        val key =
            FeatureKey(
                "Test.Embedded",
                1
            )
        val params = null
        val featureInfo =
            FeatureInfo(
                key,
                params
            )
        val configString = """{"key":{"name":"Test.Embedded","version":1}}"""
        val serializedConfiguration =
            json.decodeFromString(FeatureInfo.serializer(), configString)
        assertThat(serializedConfiguration).isEqualTo(featureInfo)
    }
}

internal inline fun <reified T : Any> FeatureParams.toDataModel(): T {
    return App.resolve<Json>().decodeFromJsonElement(this)
}

