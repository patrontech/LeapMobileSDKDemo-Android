package com.greencopper.thuzi.account.registration

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ThuziRegisteredConditionTest {

    private val localStorage: LocalStorage
    private val condition get() = ThuziRegisteredCondition(localStorage)

    init {
        Toolkit.setupTest()
        localStorage = App.resolve()

        val date = LocalDateTime.now().plusYears(1)
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.of(date, ZoneId.systemDefault()).toString()
    }

    private val json: Json = App.resolve()

    @Test
    fun whenCheckingCondition_withNullParams_shouldThrow() {
        val parameters = null
        assertThrows<IllegalArgumentException> {
            condition.check(parameters)
        }
    }

    @Test
    fun whenCheckingCondition_withWrongParams_shouldThrow() {
        assertThrows<java.lang.IllegalArgumentException> {
            condition.check(JsonNull)
        }
    }

    @Test
    fun whenCheckingCondition_withNoJWT_shouldBeFalse() {
        localStorage.project.thuzi.jwt.value = null
        val parameters = json.encodeToJsonElement(
            ThuziRegisteredCondition.ThuziRegisteredConditionData.serializer(),
            ThuziRegisteredCondition.ThuziRegisteredConditionData(true)
        )
        val value = condition.check(parameters)
        assertThat(value).isFalse
    }

    @Test
    fun whenCheckingCondition_withBlankJWT_shouldBeFalse() {
        localStorage.project.thuzi.jwt.value = "    "
        val parameters = json.encodeToJsonElement(
            ThuziRegisteredCondition.ThuziRegisteredConditionData.serializer(),
            ThuziRegisteredCondition.ThuziRegisteredConditionData(true)
        )
        val value = condition.check(parameters)
        assertThat(value).isFalse
    }

    @Test
    fun whenCheckingCondition_withJWT_shouldBeTrue() {
        localStorage.project.thuzi.jwt.value = "someValue"
        localStorage.project.thuzi.registered.value = true
        val parameters = json.encodeToJsonElement(
            ThuziRegisteredCondition.ThuziRegisteredConditionData.serializer(),
            ThuziRegisteredCondition.ThuziRegisteredConditionData(true)
        )
        val value = condition.check(parameters)
        assertThat(value).isTrue
    }

    @Test
    fun whenCheckingConditionFlow_withChangingJWT_shouldChange() {
        localStorage.project.thuzi.jwt.value = null
        val parameters = json.encodeToJsonElement(
            ThuziRegisteredCondition.ThuziRegisteredConditionData.serializer(),
            ThuziRegisteredCondition.ThuziRegisteredConditionData(true)
        )
        val conditionFlow = condition.checkFlow(parameters)
        runTest {
            var conditionValue = conditionFlow.first()
            assertThat(conditionValue).isFalse
            localStorage.project.thuzi.jwt.value = "someValue"
            localStorage.project.thuzi.registered.value = true
            conditionValue = conditionFlow.first()
            assertThat(conditionValue).isTrue
        }
    }
}
