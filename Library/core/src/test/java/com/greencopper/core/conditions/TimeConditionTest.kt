package com.greencopper.core.conditions

import com.greencopper.testmocks.core.MockTimezoneProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZonedDateTime

internal class TimeConditionTest {
    private val condition = TimeCondition(
        flowIntervalSeconds = 1,
        MockTimezoneProvider()
    )

    init {
        Toolkit.setupTest()
    }

    @Test
    fun check_whenNulls() {
        assertThrows<ParameterizedConditionException.ParamsRequired> {
            val conditionData = TimeCondition.TimeConditionData(null, null)
            condition.checkWith(conditionData)
        }
    }

    @Test
    fun check_whenNulls_Flow() {
        assertThrows<ParameterizedConditionException.ParamsRequired> {
            runTest {
                val conditionData = TimeCondition.TimeConditionData(null, null)
                condition.checkWithFlow(conditionData).first()
            }
        }
    }

    @Test
    fun check_whenOnlyFrom_WhenTrue() {
        val conditionData = TimeCondition.TimeConditionData(ZonedDateTime.now().minusDays(1).toString(), null)
        val result = condition.checkWith(conditionData)
        assertThat(result).isTrue
    }

    @Test
    fun check_whenOnlyFrom_WhenTrue_Flow() {
        runTest {
            val conditionData = TimeCondition.TimeConditionData(ZonedDateTime.now().minusDays(1).toString(), null)
            val result = condition.checkWithFlow(conditionData).first()
            assertThat(result).isTrue
        }
    }

    @Test
    fun check_whenOnlyFrom_WhenFalse() {
        val conditionData = TimeCondition.TimeConditionData(ZonedDateTime.now().plusDays(1).toString(), null)
        val result = condition.checkWith(conditionData)
        assertThat(result).isFalse
    }

    @Test
    fun check_whenOnlyTo_WhenFalse() {
        val conditionData = TimeCondition.TimeConditionData(null, ZonedDateTime.now().minusDays(1).toString())
        val result = condition.checkWith(conditionData)
        assertThat(result).isFalse
    }

    @Test
    fun check_whenOnlyTo_WhenTrue() {
        val conditionData = TimeCondition.TimeConditionData(null, ZonedDateTime.now().plusDays(1).toString())
        val result = condition.checkWith(conditionData)
        assertThat(result).isTrue
    }

    @Test
    fun check_whenToFrom_WhenTrue() {
        val conditionData = TimeCondition.TimeConditionData(
            ZonedDateTime.now().minusDays(1).toString(),
            ZonedDateTime.now().plusDays(1).toString()
        )
        val result = condition.checkWith(conditionData)
        assertThat(result).isTrue
    }

    @Test
    fun check_whenToFrom_intervalBefore_WhenFalse() {
        val conditionData = TimeCondition.TimeConditionData(
            ZonedDateTime.now().minusDays(2).toString(),
            ZonedDateTime.now().minusDays(1).toString()
        )
        val result = condition.checkWith(conditionData)
        assertThat(result).isFalse
    }

    @Test
    fun check_whenToFrom_intervalAfter_WhenFalse() {
        val conditionData = TimeCondition.TimeConditionData(
            ZonedDateTime.now().plusDays(1).toString(),
            ZonedDateTime.now().plusDays(2).toString()
        )
        val result = condition.checkWith(conditionData)
        assertThat(result).isFalse
    }

    @Test
    fun check_whenToFrom_IncorrectInterval_WhenFalse() {
        val conditionData = TimeCondition.TimeConditionData(
            ZonedDateTime.now().minusDays(1).toString(),
            ZonedDateTime.now().minusDays(2).toString()
        )
        val result = condition.checkWith(conditionData)
        assertThat(result).isFalse
    }

    @Test
    fun testDeserialize() {
        Toolkit.setupTest()

        val fromDate = ZonedDateTime.now().plusDays(1)
        val data = TimeCondition.TimeConditionData(fromDate.toString(), null)
            .encodeToJsonElement()
        val conditionData = condition.deserialize(data)
        assertThat(conditionData.fromDate)
            .isEqualTo(fromDate)
        assertThat(conditionData.toDate).isNull()
    }

    @Test
    fun testParseException() {
        val conditionData = TimeCondition.TimeConditionData("123", "123")
        assertThat(conditionData.fromDate).isNull()
        assertThat(conditionData.toDate).isNull()
    }

    @Test
    fun testSerializableData() = testKiboSerializable(
        TimeCondition.TimeConditionData(ZonedDateTime.now().toString(), ZonedDateTime.now().toString()),
    )
}
