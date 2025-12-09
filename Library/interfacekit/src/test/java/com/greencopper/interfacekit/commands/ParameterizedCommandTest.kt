package com.greencopper.interfacekit.commands

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.commands.system.CommandParameters
import com.greencopper.interfacekit.commands.system.ParameterizedCommand
import com.greencopper.interfacekit.commands.system.ParameterizedCommandException
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ParameterizedCommandTest {

    init {
        Toolkit.setupTest()
    }

    @Serializable
    private data class BooleanData(val boolean: Boolean): KiboSerializable<BooleanData> {
        override fun getSerializer(): KSerializer<BooleanData> = serializer()
    }

    private class CommandThrowException(private val exception: Exception) : ParameterizedCommand<BooleanData>() {
        override fun executeWith(params: BooleanData, origin: Layout?) = throw exception
        override fun deserialize(commandParameters: CommandParameters): BooleanData = throw exception
    }

    private class CommandParameterBoolean : ParameterizedCommand<BooleanData>() {
        var executed = false
        var parameter: Boolean? = null

        override fun executeWith(params: BooleanData, origin: Layout?): Flow<Boolean> {
            this.executed = true
            this.parameter = params.boolean

            return flowOf(true)
        }

        override fun deserialize(commandParameters: CommandParameters): BooleanData = KiboSerializable.decodeFromJsonElement(commandParameters)
    }

    @Test
    fun whenCheckingParameterizedCommand_withNull_shouldThrow() {
        val commandTyped = CommandParameterBoolean()
        assertThrows<ParameterizedCommandException.ParamsRequired> { commandTyped.execute(null) }
    }

    @Test
    fun whenCheckingParameterizedCommand_withInvalidJson_shouldThrow() {
        val commandTyped = CommandParameterBoolean()
        assertThrows<ParameterizedCommandException.ParseErrorException> { commandTyped.execute(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedCommand_withInvalidJson_shouldThrow_withDefaultMessage() {
        val commandTyped = CommandThrowException(SerializationException(message = null))
        assertThrows<ParameterizedCommandException.ParseErrorException> { commandTyped.execute(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedCommand_withInvalidClassSerializer_shouldThrow_withDefaultMessage() {
        val commandTyped = CommandThrowException(ClassCastException(null))
        assertThrows<ParameterizedCommandException.ParseErrorException> { commandTyped.execute(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedCommand_withInvalidClassSerializer_shouldThrow_withMessage() {
        val commandTyped = CommandThrowException(ClassCastException("Test Message"))
        assertThrows<ParameterizedCommandException.ParseErrorException> { commandTyped.execute(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedCommand_withJsonValidAndTrue_shouldBeTrue() {
        val commandTyped = CommandParameterBoolean()
        val booleanParameter = BooleanData(true)
        val jsonBooleanParameter = booleanParameter.encodeToJsonElement()
        commandTyped.execute(jsonBooleanParameter)
        assertThat(commandTyped.parameter).isTrue
    }

    @Test
    fun whenCheckingParameterizedCommand_withTypedTrue_shouldBeTrue() {
        val commandTyped = CommandParameterBoolean()
        val booleanParameter = BooleanData(true)
        commandTyped.executeWith(booleanParameter, null)
        assertThat(commandTyped.parameter).isTrue
    }

    @Test
    fun whenCheckingParameterizedCommand_withJsonValidAndFalse_shouldBeFalse() {
        val commandTyped = CommandParameterBoolean()
        val booleanParameter = BooleanData(false)
        val jsonBooleanParameter = booleanParameter.encodeToJsonElement()
        commandTyped.execute(jsonBooleanParameter)
        assertThat(commandTyped.parameter).isFalse
    }

    @Test
    fun whenCheckingParameterizedCommand_withTypedFalse_shouldBeFalse() {
        val commandTyped = CommandParameterBoolean()
        val booleanParameter = BooleanData(false)
        commandTyped.executeWith(booleanParameter, null)
        assertThat(commandTyped.parameter).isFalse
    }

    @Test
    fun whenCheckingFlowParameterizedCommand_withInvalidClassSerializer_shouldThrow() {
        val commandTyped = CommandThrowException(ClassCastException(null))
        assertThrows<ParameterizedCommandException.ParseErrorException> { commandTyped.execute(JsonNull) }
    }
}
