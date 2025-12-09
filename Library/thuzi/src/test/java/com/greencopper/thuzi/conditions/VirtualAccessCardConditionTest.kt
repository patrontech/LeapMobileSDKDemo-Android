package com.greencopper.thuzi.conditions

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.localstorage.ThuziState
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.setAuthenticated
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class VirtualAccessCardConditionTest {
    private val localStorage: LocalStorage
    private val condition: VirtualAccessCardCondition

    init {
        Toolkit.setupTest()
        localStorage = App.resolve()
        condition = VirtualAccessCardCondition(localStorage)
        setAuthenticated(localStorage, true)
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
    fun whenCheckingCondition_withNullThuziState_shouldBeFalse() {
        val parameter = VirtualAccessCardCondition.VirtualAccessCardConditionData("1")
        Assertions.assertThat(condition.check(parameter.encodeToJsonElement())).isFalse
    }

    @Test
    fun whenCheckingCondition_withNullThuziState_Flow_shouldBeFalse() {
        val parameter = VirtualAccessCardCondition.VirtualAccessCardConditionData("1")
        val conditionFlow = condition.checkWithFlow(parameter)
        runTest {
            val result = conditionFlow.first()
            Assertions.assertThat(result).isFalse
        }
    }

    @Test
    fun whenCheckingCondition_withNullVACList_shouldBeFalse() {
        val parameter = VirtualAccessCardCondition.VirtualAccessCardConditionData("1")
        localStorage.project.thuzi.state.value = ThuziState(virtualAccessCards = null)
        Assertions.assertThat(condition.check(parameter.encodeToJsonElement())).isFalse
    }

    @Test
    fun whenCheckingCondition_withNullVACList_Flow_shouldBeFalse() {
        val parameter = VirtualAccessCardCondition.VirtualAccessCardConditionData("1")
        localStorage.project.thuzi.state.value = ThuziState(virtualAccessCards = null)
        val conditionFlow = condition.checkWithFlow(parameter)
        runTest {
            val result = conditionFlow.first()
            Assertions.assertThat(result).isFalse
        }
    }

    @Test
    fun whenCheckingCondition_withMissingVAC_shouldBeFalse() {
        val parameter = VirtualAccessCardCondition.VirtualAccessCardConditionData("1")
        localStorage.project.thuzi.state.value = ThuziState(virtualAccessCards = listOf("2", "3"))
        Assertions.assertThat(condition.check(parameter.encodeToJsonElement())).isFalse
    }

    @Test
    fun whenCheckingCondition_withMissingVAC_Flow_shouldBeFalse() {
        val parameter = VirtualAccessCardCondition.VirtualAccessCardConditionData("1")
        localStorage.project.thuzi.state.value = ThuziState(virtualAccessCards = listOf("2", "3"))
        val conditionFlow = condition.checkWithFlow(parameter)
        runTest {
            val result = conditionFlow.first()
            Assertions.assertThat(result).isFalse
        }
    }

    @Test
    fun whenCheckingCondition_withExistingVAC_shouldBeTrue() {
        val parameter = VirtualAccessCardCondition.VirtualAccessCardConditionData("1")
        localStorage.project.thuzi.state.value = ThuziState(virtualAccessCards = listOf("1", "2", "3"))
        Assertions.assertThat(condition.check(parameter.encodeToJsonElement())).isTrue
    }

    @Test
    fun whenCheckingCondition_withMissingVAC_Flow_shouldBeTrue() {
        val parameter = VirtualAccessCardCondition.VirtualAccessCardConditionData("1")
        localStorage.project.thuzi.state.value = ThuziState(virtualAccessCards = listOf("1", "2", "3"))
        val conditionFlow = condition.checkWithFlow(parameter)
        runTest {
            val result = conditionFlow.first()
            Assertions.assertThat(result).isTrue
        }
    }
}