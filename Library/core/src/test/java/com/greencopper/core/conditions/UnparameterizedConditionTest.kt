package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.UnparameterizedCondition
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UnparameterizedConditionTest {
    private class TestUnparameterizedCondition : UnparameterizedCondition() {
        override fun check(): Boolean = true
        override fun checkFlow(): Flow<Boolean> = flowOf(true)
    }

    init {
        Toolkit.setupTest()
    }

    @Test
    fun whenCheckingConditionUnparameterized_WithRandomParameters_resultShouldNotChange() {
        val unparameterizedCondition = TestUnparameterizedCondition()
        val randomConditionParameters = App.resolve<Json>().encodeToJsonElement(Boolean.serializer(), false)
        assertThat(
                unparameterizedCondition.check() == unparameterizedCondition.check(randomConditionParameters)
        ).isTrue
    }

    @Test
    fun whenCheckingConditionUnparameterized_WithNullParameters_resultShouldNotChange() {
        val unparameterizedCondition = TestUnparameterizedCondition()
        assertThat(
                unparameterizedCondition.check() == unparameterizedCondition.check(null)
        ).isTrue
    }

    @Test
    fun whenCheckingConditionUnparameterized_WithJsonNullParameters_resultShouldNotChange() {
        val unparameterizedCondition = TestUnparameterizedCondition()
        assertThat(
                unparameterizedCondition.check() == unparameterizedCondition.check(JsonNull)
        ).isTrue
    }

    @Test
    fun whenCheckingFlowConditionUnparameterized_WithRandomParameters_resultShouldNotChange() {
        val unparameterizedCondition = TestUnparameterizedCondition()
        val randomConditionParameters = App.resolve<Json>().encodeToJsonElement(Boolean.serializer(), false)

        runTest {
            val validityNoParameter = unparameterizedCondition.checkFlow().first()
            val validityRandomParameter = unparameterizedCondition.checkFlow(randomConditionParameters).first()
            assertThat(validityNoParameter == validityRandomParameter).isTrue
        }
    }

    @Test
    fun whenCheckingFlowConditionUnparameterized_WithNullParameters_resultShouldNotChange() {
        val unparameterizedCondition = TestUnparameterizedCondition()

        runTest {
            val validityNoParameter = unparameterizedCondition.checkFlow().first()
            val validityNullParameter = unparameterizedCondition.checkFlow(null).first()
            assertThat(validityNoParameter == validityNullParameter).isTrue
        }
    }

    @Test
    fun whenCheckingFlowConditionUnparameterized_WithJsonNullParameters_resultShouldNotChange() {
        val unparameterizedCondition = TestUnparameterizedCondition()

        runTest {
            val validityNoParameter = unparameterizedCondition.checkFlow().first()
            val validityJsonNullParameter = unparameterizedCondition.checkFlow(JsonNull).first()
            assertThat(validityNoParameter == validityJsonNullParameter).isTrue
        }
    }
}