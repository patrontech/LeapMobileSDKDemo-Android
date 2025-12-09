package com.greencopper.core.conditions.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class IdTest {

    @Test
    fun check_whenCondition() {
        val conditionsMap: ConditionsMap = mapOf("testValue" to { true })
        val id = Id("testValue")
        assertThat(id.check(conditionsMap)).isTrue
    }

    @Test
    fun check_whenConditionFalse() {
        val conditionsMap: ConditionsMap = mapOf("testValue" to { false })
        val id = Id("testValue")
        assertThat(id.check(conditionsMap)).isFalse
    }
}