package com.greencopper.thuzi.conditions

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.localstorage.ThuziState
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZonedDateTime

internal class CustomAnswerConditionTest {
    private lateinit var lazyLocalStorage: LazyResolver<LocalStorage>
    private lateinit var condition: CustomAnswerCondition

    @BeforeEach
    internal fun beforeEachTest() {
        Toolkit.setupTest()
        lazyLocalStorage = LazyResolver.adhoc(LocalStorage("project"))
        lazyLocalStorage.resolve().project.thuzi.jwtExpirationDate.value = ZonedDateTime.now().plusDays(1).toString()
        condition = CustomAnswerCondition(lazyLocalStorage)
    }

    @Test
    fun whenCheckingCondition_withNullParams_shouldThrow() {
        val parameters = null
        assertThrows<IllegalArgumentException> {
            condition.check(parameters)
        }
    }

    @Test
    fun whenCheckingCondition_withWrongParams_shouldThrow() {
        assertThrows<IllegalArgumentException> {
            condition.check(JsonNull)
        }
    }

    @Test
    fun whenCheckingCondition_withMissingAnswer_shouldBeFalse() {
        val parameters = CustomAnswerCondition.CustomAnswerConditionData(
            answer = "3",
            method = CustomAnswerCondition.Method.EQUALS,
            negated = false,
            caseInsensitive = true,
            pattern = "fnord!"
        )
        assertThat(condition.check(parameters.encodeToJsonElement())).isFalse
    }

    @Test
    fun whenCheckingCondition_negatedWithMissingAnswer_shouldBeTrue() {
        val parameters = CustomAnswerCondition.CustomAnswerConditionData(
            answer = "3",
            method = CustomAnswerCondition.Method.EQUALS,
            negated = true,
            caseInsensitive = true,
            pattern = "fnord!"
        )
        assertThat(condition.check(parameters.encodeToJsonElement())).isTrue
    }

    @Test
    fun whenCheckingCondition_matchingWithCaseInsensitiveEquals_shouldBeTrue() {
        val parameters = CustomAnswerCondition.CustomAnswerConditionData(
            answer = "0",
            method = CustomAnswerCondition.Method.EQUALS,
            negated = false,
            caseInsensitive = true,
            pattern = "fnord!"
        )
        val state = ThuziState(answers = mapOf("0" to "fNorD!"))
        lazyLocalStorage.resolve().project.thuzi.state.value = state
        assertThat(condition.check(parameters.encodeToJsonElement())).isTrue
    }

    @Test
    fun whenCheckingCondition_notMatchingWithCaseSensitiveEquals_shouldBeFalse() {
        val parameters = CustomAnswerCondition.CustomAnswerConditionData(
            answer = "0",
            method = CustomAnswerCondition.Method.EQUALS,
            negated = false,
            caseInsensitive = false,
            pattern = "fnord!"
        )
        val state = ThuziState(answers = mapOf("0" to "fNorD!"))
        lazyLocalStorage.resolve().project.thuzi.state.value = state
        assertThat(condition.check(parameters.encodeToJsonElement())).isFalse
    }

    @Test
    fun whenCheckingCondition_matchingWithCaseInsensitiveContains_shouldBeTrue() {
        val parameters = CustomAnswerCondition.CustomAnswerConditionData(
            answer = "1",
            method = CustomAnswerCondition.Method.CONTAINS,
            negated = false,
            caseInsensitive = true,
            pattern = "nor"
        )
        val state = ThuziState(answers = mapOf("1" to "fNorD!"))
        lazyLocalStorage.resolve().project.thuzi.state.value = state
        assertThat(condition.check(parameters.encodeToJsonElement())).isTrue
    }

    @Test
    fun whenCheckingConditionFlow_withChangingAnswers_shouldChange() {
        val parameters = CustomAnswerCondition.CustomAnswerConditionData(
            answer = "1",
            method = CustomAnswerCondition.Method.CONTAINS,
            negated = false,
            caseInsensitive = true,
            pattern = "nor"
        )
        val conditionFlow = condition.checkFlow(parameters.encodeToJsonElement())
        runTest {
            var value = conditionFlow.first()
            assertThat(value).isFalse
            val state = ThuziState(answers = mapOf("1" to "fNorD!"))
            lazyLocalStorage.resolve().project.thuzi.state.value = state
            value = conditionFlow.first()
            assertThat(value).isTrue
        }
    }
}