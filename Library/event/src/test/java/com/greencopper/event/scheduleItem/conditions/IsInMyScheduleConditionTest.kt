package com.greencopper.event.scheduleItem.conditions

import com.greencopper.eventmocks.MockMyScheduleManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class IsInMyScheduleConditionTest {

    private val myScheduleItemIds = setOf(1L, 2L, 3L)
    private val condition = IsInMyScheduleCondition(MockMyScheduleManager(myScheduleItemIds))

    @Test
    @DisplayName("Given wrong item id, When checkWith is called, Then it should return false")
    fun checkWithShouldReturnFalse() {
        val result = condition.checkWith(IsInMyScheduleCondition.MyScheduleData(4))
        assertThat(result).isFalse
    }

    @Test
    @DisplayName("Given wrong item id, When checkWithFlow is called, Then it should return false")
    fun checkWithFlowShouldReturnFalse() {
        runTest {
            val result = condition
                .checkWithFlow(IsInMyScheduleCondition.MyScheduleData(4))
                .first()
            assertThat(result).isFalse
        }
    }

    @Test
    @DisplayName("Given correct item id, When checkWith is called, Then it should return true")
    fun checkWithShouldReturnTrue() {
        val result = condition.checkWith(IsInMyScheduleCondition.MyScheduleData(1))
        assertThat(result).isTrue
    }

    @Test
    @DisplayName("Given correct item id, When checkWithFlow is called, Then it should return true")
    fun checkWithFlowShouldReturnTrue() {
        runTest {
            val result = condition
                .checkWithFlow(IsInMyScheduleCondition.MyScheduleData(2))
                .first()
            assertThat(result).isTrue
        }
    }
}
