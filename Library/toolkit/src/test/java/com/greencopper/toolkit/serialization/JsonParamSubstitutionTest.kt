package com.greencopper.toolkit.serialization

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class JsonParamSubstitutionTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()
    private val paramString = "paramStringToReplace"
    private val paramInt = "paramIntToReplace"

    private val paramsSubstitutes = mapOf(
        paramString to JsonPrimitive("paramStringValue"),
        paramInt to JsonPrimitive(42)
    )

    private val baseJson = buildJsonObject {
        put("myParamString", JsonPrimitive("{#/$paramString}"))
        put("myParamInt", JsonPrimitive("{#/$paramInt?}"))
    }

    @Test
    fun withoutParams_withQueryParams_shouldReturnUnchanged() {
        val jsonToWork = buildJsonObject {
            buildJsonArray {
                add("pouet")
                add("test")
            }
            buildJsonArray {
                add(buildJsonObject { put("test", JsonPrimitive("value")) })
                add(buildJsonObject { put("test", JsonPrimitive("value2")) })
            }
            put("key", JsonPrimitive(100))
        }
        val result = jsonToWork.substituteJsonParams(paramsSubstitutes)
        assertThat(result).isEqualTo(jsonToWork)
    }

    @Test
    fun withMalformedParams_withQueryParams_shouldReturnUnchanged() {
        val jsonToWork = buildJsonObject { put("myParamString", JsonPrimitive("{#/test[ey?}")) }
        val result = jsonToWork.substituteJsonParams(paramsSubstitutes)
        assertThat(result).isEqualTo(jsonToWork)
    }

    @Test
    fun withParams_withoutValidQueryParams_shouldThrow() {
        assertThrows<JsonSubstitutionException.MissingKey> {
            baseJson.substituteJsonParams(mapOf("test" to JsonPrimitive("pouet")))
        }
    }

    @Test
    fun withOptionalParams_withoutQueryParams_shouldReturnChangedToNull() {
        val json = buildJsonObject { put("myParamInt", JsonPrimitive("{#/$paramInt?}")) }
        val result = json.substituteJsonParams(emptyMap())
        assertThat(result.toString()).contains("\"myParamInt\":null")
    }

    @Test
    fun withParams_withQueryParams_shouldReturnChanged() {
        val result = baseJson.substituteJsonParams(paramsSubstitutes)
        assertThat(result.toString()).contains("\"myParamString\":\"paramStringValue\"")
        assertThat(result.toString()).contains("\"myParamInt\":42")
    }

    @Test
    fun withJsonObjectParams_shouldReplaceStringWithJsonObject() {
        val originalJson = buildJsonObject { put("param", JsonPrimitive("{#/param}")) }
        val withParams: Map<String, JsonElement> = mapOf(
            "param" to buildJsonObject { put("newParam", JsonPrimitive("newParam")) }
        )

        val expectedResult = buildJsonObject {
            put("param", buildJsonObject { put("newParam", JsonPrimitive("newParam")) })
        }

        val result = originalJson.substituteJsonParams(withParams)

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun stringWithPartialSubstitution_shouldReplaceString() {
        val originalJson = buildJsonObject { put("param", JsonPrimitive("TAG {#/param}")) }
        val withParams: Map<String, JsonElement> = mapOf("param" to JsonPrimitive("newParam"))

        val expectedResult = buildJsonObject { put("param", JsonPrimitive("TAG newParam")) }

        val result = originalJson.substituteJsonParams(withParams)

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun stringJsonElement_shouldReplaceString() {
        val value = "stringValue"
        val withParams: Map<String, JsonElement> = mapOf(paramString to json.parseToJsonElement(value))
        val originalJson = buildJsonObject { put("param", JsonPrimitive("{#/$paramString}")) }

        val expectedResult = buildJsonObject { put("param", JsonPrimitive(value)) }

        val result = originalJson.substituteJsonParams(withParams)

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun manyElementTypes_shouldReplace() {
        val originalJson = buildJsonObject {
            put("stringParam", JsonPrimitive("{#/stringParamValue}"))
            put("boolParam", JsonPrimitive("{#/boolParamValue}"))
            put("intParam", JsonPrimitive("{#/intParamValue}"))
            put("floatParam", JsonPrimitive("{#/floatParamValue}"))
        }
        val withParams: Map<String, JsonElement> = mapOf(
            "stringParamValue" to json.encodeToJsonElement("qwerty"),
            "boolParamValue" to json.encodeToJsonElement(true),
            "intParamValue" to json.encodeToJsonElement(50),
            "floatParamValue" to json.encodeToJsonElement(1.2345),
        )

        val expectedResult = buildJsonObject {
            put("stringParam", JsonPrimitive("qwerty"))
            put("boolParam", JsonPrimitive(true))
            put("intParam", JsonPrimitive(50))
            put("floatParam", JsonPrimitive(1.2345))
        }

        val result = originalJson.substituteJsonParams(withParams)

        assertThat(result).isEqualTo(expectedResult)
    }
}