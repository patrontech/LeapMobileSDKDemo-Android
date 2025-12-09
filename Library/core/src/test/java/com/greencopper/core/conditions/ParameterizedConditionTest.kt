package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ParameterizedConditionTest {

    init {
        Toolkit.setupTest()
    }

    @Serializable
    private data class BooleanData(val boolean: Boolean): KiboSerializable<BooleanData> {
        override fun getSerializer(): KSerializer<BooleanData> = serializer()
    }

    private class ConditionThrowException(private val exception: Exception) : ParameterizedCondition<BooleanData>() {
        override fun checkWith(parameter: BooleanData): Boolean = parameter.boolean
        override fun checkWithFlow(parameter: BooleanData): Flow<Boolean> = flowOf(parameter.boolean)
        override fun deserialize(conditionParameters: ConditionParameters): BooleanData = throw exception
    }

    private class ConditionIsParameterBooleanTrue : ParameterizedCondition<BooleanData>() {
        override fun checkWith(parameter: BooleanData): Boolean = parameter.boolean
        override fun checkWithFlow(parameter: BooleanData): Flow<Boolean> = flowOf(parameter.boolean)
        override fun deserialize(conditionParameters: ConditionParameters): BooleanData = KiboSerializable.decodeFromJsonElement(conditionParameters)
    }

    @Test
    fun whenCheckingParameterizedCondition_withNull_shouldThrow() {
        val conditionTyped = ConditionIsParameterBooleanTrue()
        assertThrows<ParameterizedConditionException.ParamsRequired> { conditionTyped.check(null) }
    }

    @Test
    fun whenCheckingParameterizedCondition_withInvalidJson_shouldThrow() {
        val conditionTyped = ConditionIsParameterBooleanTrue()
        assertThrows<ParameterizedConditionException.ParseErrorException> { conditionTyped.check(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedCondition_withInvalidJson_shouldThrow_withDefaultMessage() {
        val conditionTyped = ConditionThrowException(SerializationException(message = null))
        assertThrows<ParameterizedConditionException.ParseErrorException> { conditionTyped.check(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedCondition_withInvalidClassSerializer_shouldThrow_withDefaultMessage() {
        val conditionTyped = ConditionThrowException(ClassCastException(null))
        assertThrows<ParameterizedConditionException.ParseErrorException> { conditionTyped.check(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedCondition_withInvalidClassSerializer_shouldThrow_withMessage() {
        val conditionTyped = ConditionThrowException(ClassCastException("Test Message"))
        assertThrows<ParameterizedConditionException.ParseErrorException> { conditionTyped.check(JsonNull) }
    }

    @Test
    fun whenCheckingParameterizedCondition_withJsonValidAndTrue_shouldBeTrue() {
        val conditionTyped = ConditionIsParameterBooleanTrue()
        val booleanParameter = BooleanData(true)
        val jsonBooleanParameter = booleanParameter.encodeToJsonElement()
        assertThat(conditionTyped.check(jsonBooleanParameter)).isTrue
    }

    @Test
    fun whenCheckingParameterizedCondition_withTypedTrue_shouldBeTrue() {
        val conditionTyped = ConditionIsParameterBooleanTrue()
        val booleanParameter = BooleanData(true)
        assertThat(conditionTyped.checkWith(booleanParameter)).isTrue
    }

    @Test
    fun whenCheckingParameterizedCondition_withJsonValidAndFalse_shouldBeFalse() {
        val conditionTyped = ConditionIsParameterBooleanTrue()
        val booleanParameter = BooleanData(false)
        val jsonBooleanParameter = booleanParameter.encodeToJsonElement()
        assertThat(conditionTyped.check(jsonBooleanParameter)).isFalse
    }

    @Test
    fun whenCheckingParameterizedCondition_withTypedFalse_shouldBeTrue() {
        val conditionTyped = ConditionIsParameterBooleanTrue()
        val booleanParameter = BooleanData(false)
        assertThat(conditionTyped.checkWith(booleanParameter)).isFalse
    }

    @Test
    fun whenCheckingFlowParameterizedCondition_withNull_shouldThrow() {
        val conditionTyped = ConditionIsParameterBooleanTrue()
        assertThrows<ParameterizedConditionException.ParamsRequired> { conditionTyped.checkFlow(null) }
    }

    @Test
    fun whenCheckingFlowParameterizedCondition_withInvalidJson_shouldThrow() {
        val conditionTyped = ConditionIsParameterBooleanTrue()
        assertThrows<ParameterizedConditionException.ParseErrorException> { conditionTyped.checkFlow(JsonNull) }
    }

    @Test
    fun whenCheckingFlowParameterizedCondition_withInvalidClassSerializer_shouldThrow() {
        val conditionTyped = ConditionThrowException(ClassCastException(null))
        assertThrows<ParameterizedConditionException.ParseErrorException> { conditionTyped.checkFlow(JsonNull) }
    }

    @Test
    fun whenCheckingFlowParameterizedCondition_withJsonValidAndTrue_shouldBeTrue() {
        val conditionTyped = ConditionIsParameterBooleanTrue()
        val booleanParameter = BooleanData(true)
        val jsonBooleanParameter = booleanParameter.encodeToJsonElement()
        runTest {
            val validity = conditionTyped.checkFlow(jsonBooleanParameter).first()
            assertThat(validity).isTrue
        }
    }

    @Test
    fun whenCheckingFlowParameterizedCondition_withJsonValidAndFalse_shouldBeFalse() {
        val conditionTyped = ConditionIsParameterBooleanTrue()
        val booleanParameter = BooleanData(false)
        val jsonBooleanParameter = booleanParameter.encodeToJsonElement()
        runTest {
            val validity = conditionTyped.checkFlow(jsonBooleanParameter).first()
            assertThat(validity).isFalse
        }
    }
}