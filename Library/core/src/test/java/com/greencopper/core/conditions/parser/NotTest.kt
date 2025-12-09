package com.greencopper.core.conditions.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class NotTest {
    @Test
    fun test_whenCondition() {
        val conditionsMap: ConditionsMap = mapOf("testValue" to { false })
        val predicate = Not(Id("testValue"))
        assertThat(predicate.check(conditionsMap)).isTrue
    }

    @Test
    fun test_whenConditionFalse() {
        val conditionsMap: ConditionsMap = mapOf("testValue" to { true })
        val predicate = Not(Id("testValue"))
        assertThat(predicate.check(conditionsMap)).isFalse
    }
}