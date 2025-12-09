package com.greencopper.core.automation

import com.greencopper.core.data.KiboSerializable
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ParameterizedAutomationTest {
    
    init {
        Toolkit.setupTest()
    }

    @Serializable
    private data class BooleanData(val boolean: Boolean) : KiboSerializable<BooleanData> {
        override fun getSerializer(): KSerializer<BooleanData> = serializer()
    }

    private class AutomationThrowException(private val exception: Exception) : ParameterizedAutomation<BooleanData>() {
        override fun setupWith(params: BooleanData) = throw exception
        override fun deserialize(automationParameters: AutomationParams): BooleanData = throw exception
    }

    private class AutomationParameterBoolean : ParameterizedAutomation<BooleanData>() {
        var executed = false
        var parameter: Boolean? = null

        override fun setupWith(params: BooleanData) {
            this.executed = true
            this.parameter = params.boolean
        }

        override fun deserialize(automationParameters: AutomationParams): BooleanData =
            KiboSerializable.decodeFromJsonElement(automationParameters)
    }

    @Test
    fun whenCheckingParameterizedAutomation_withNull_shouldThrow() {
        val automationTyped = AutomationParameterBoolean()
        org.junit.jupiter.api.assertThrows<ParameterizedAutomationException.ParamsRequired> { automationTyped.setup(null) }
    }

    @Test
    fun whenCheckingParameterizedAutomation_withInvalidJson_shouldThrow() {
        val automationTyped = AutomationParameterBoolean()
        org.junit.jupiter.api.assertThrows<ParameterizedAutomationException.ParseErrorException> { automationTyped.setup(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedAutomation_withInvalidJson_shouldThrow_withDefaultMessage() {
        val automationTyped = AutomationThrowException(SerializationException(message = null))
        org.junit.jupiter.api.assertThrows<ParameterizedAutomationException.ParseErrorException> { automationTyped.setup(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedAutomation_withInvalidClassSerializer_shouldThrow_withDefaultMessage() {
        val automationTyped = AutomationThrowException(ClassCastException(null))
        org.junit.jupiter.api.assertThrows<ParameterizedAutomationException.ParseErrorException> { automationTyped.setup(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedAutomation_withInvalidClassSerializer_shouldThrow_withMessage() {
        val automationTyped = AutomationThrowException(ClassCastException("Test Message"))
        org.junit.jupiter.api.assertThrows<ParameterizedAutomationException.ParseErrorException> { automationTyped.setup(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedAutomation_withJsonValidAndTrue_shouldBeTrue() {
        val automationTyped = AutomationParameterBoolean()
        val booleanParameter = BooleanData(true)
        val jsonBooleanParameter = booleanParameter.encodeToJsonElement()
        automationTyped.setup(jsonBooleanParameter)
        assertThat(automationTyped.parameter).isTrue
    }

    @Test
    fun whenCheckingParameterizedAutomation_withTypedTrue_shouldBeTrue() {
        val automationTyped = AutomationParameterBoolean()
        val booleanParameter = BooleanData(true)
        automationTyped.setupWith(booleanParameter)
        assertThat(automationTyped.parameter).isTrue
    }

    @Test
    fun whenCheckingParameterizedAutomation_withJsonValidAndFalse_shouldBeFalse() {
        val automationTyped = AutomationParameterBoolean()
        val booleanParameter = BooleanData(false)
        val jsonBooleanParameter = booleanParameter.encodeToJsonElement()
        automationTyped.setup(jsonBooleanParameter)
        assertThat(automationTyped.parameter).isFalse
    }

    @Test
    fun whenCheckingParameterizedAutomation_withTypedFalse_shouldBeFalse() {
        val automationTyped = AutomationParameterBoolean()
        val booleanParameter = BooleanData(false)
        automationTyped.setupWith(booleanParameter)
        assertThat(automationTyped.parameter).isFalse
    }

    @Test
    fun whenCheckingFlowParameterizedAutomation_withInvalidClassSerializer_shouldThrow() {
        val automationTyped = AutomationThrowException(ClassCastException(null))
        org.junit.jupiter.api.assertThrows<ParameterizedAutomationException.ParseErrorException> { automationTyped.setup(JsonNull) }
    }
}
