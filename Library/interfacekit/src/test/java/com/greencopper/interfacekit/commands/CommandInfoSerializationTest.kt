package com.greencopper.interfacekit.commands

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class CommandInfoSerializationTest {
    @BeforeEach
    fun setUp() {
        Toolkit.setupTest()
    }

    @Test
    fun serializeAndDeserializeTestWithParams_shouldNotThrow() {
        assertDoesNotThrow {
            val data = KiboSerializable.decodeFromString<CommandInfo>(jsonStringWithParams)
            data.checkWithParams()

            val dataBis = KiboSerializable.decodeFromString<CommandInfo>(data.encodeToString())
            dataBis.checkWithParams()

            val dataAgain = CommandInfo(
                CommandInfo.Key("CommandInfo", 1),
                JsonObject(content = mapOf(
                    Pair("param", JsonPrimitive("val"))
                ))
            )
            dataAgain.checkWithParams()
        }
    }

    @Test
    fun serializeAndDeserializeTestWithoutParams_shouldNotThrow() {
        assertDoesNotThrow {
            val data = KiboSerializable.decodeFromString<CommandInfo>(jsonStringWithoutParams)
            data.checkWithoutParams()

            val dataBis = KiboSerializable.decodeFromString<CommandInfo>(data.encodeToString())
            dataBis.checkWithoutParams()

            val dataAgain = CommandInfo(
                CommandInfo.Key("CommandInfo", 1)
            )
            dataAgain.checkWithoutParams()
        }
    }

    @Test
    fun serializeAndDeserializeTest_shouldNotThrow() {
        assertDoesNotThrow {
            val data = CommandInfo(
                CommandInfo.Key("CommandInfo", 1),
                JsonObject(content = mapOf(
                    Pair("param", JsonPrimitive("val"))
                ))
            )
            assertThat(data).isEqualTo(data.copy())
        }
    }

    private fun CommandInfo.checkWithParams() {
        checkWithoutParams()
        assertThat(params?.jsonObject?.get("param")?.jsonPrimitive?.content).isEqualTo("val")
    }

    private fun CommandInfo.checkWithoutParams() {
        assertThat(key.name).isEqualTo("CommandInfo")
        assertThat(key.version).isEqualTo(1)
    }

    private val jsonStringWithParams = "{\n" +
            "   \"key\":{\n" +
            "      \"name\":\"CommandInfo\",\n" +
            "      \"version\":1\n" +
            "   },\n" +
            "   \"params\":{\n" +
            "      \"param\":\"val\"\n" +
            "   }\n" +
            "}"

    private val jsonStringWithoutParams = "{\n" +
            "   \"key\":{\n" +
            "      \"name\":\"CommandInfo\",\n" +
            "      \"version\":1\n" +
            "   }" +
            "}"
}
