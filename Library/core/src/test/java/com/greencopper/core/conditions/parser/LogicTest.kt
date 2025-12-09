package com.greencopper.core.conditions.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LogicTest {
    @Test
    fun test_AndConditionBothTrue() {
        val conditions: ConditionsMap = mapOf("left" to { true }, "right" to { true })
        val predicate = Logic(Id("left"), Op.AND, Id("right"))
        assertThat(predicate.check(conditions)).isTrue
    }

    @Test
    fun test_AndConditionOneTrue() {
        val conditions: ConditionsMap = mapOf("left" to { true }, "right" to { false })
        val predicate = Logic(Id("left"), Op.AND, Id("right"))
        assertThat(predicate.check(conditions)).isFalse
    }

    @Test
    fun test_AndConditionBothFalse() {
        val conditions: ConditionsMap = mapOf("left" to { false }, "right" to { false })
        val predicate = Logic(Id("left"), Op.AND, Id("right"))
        assertThat(predicate.check(conditions)).isFalse
    }

    @Test
    fun test_OrConditionBothTrue() {
        val conditions: ConditionsMap = mapOf("left" to { true }, "right" to { true })
        val predicate = Logic(Id("left"), Op.OR, Id("right"))
        assertThat(predicate.check(conditions)).isTrue
    }

    @Test
    fun test_OrConditionBothFalse() {
        val conditions: ConditionsMap = mapOf("left" to { false }, "right" to { false })
        val predicate = Logic(Id("left"), Op.OR, Id("right"))
        assertThat(predicate.check(conditions)).isFalse
    }

    @Test
    fun test_OrConditionLeftTrue() {
        val conditions: ConditionsMap = mapOf("left" to { true }, "right" to { false })
        val predicate = Logic(Id("left"), Op.OR, Id("right"))
        assertThat(predicate.check(conditions)).isTrue
    }

    @Test
    fun test_OrConditionRightTrue() {
        val conditions: ConditionsMap = mapOf("left" to { false }, "right" to { true })
        val predicate = Logic(Id("left"), Op.OR, Id("right"))
        assertThat(predicate.check(conditions)).isTrue
    }
}