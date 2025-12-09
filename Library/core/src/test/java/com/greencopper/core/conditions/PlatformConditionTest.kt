package com.greencopper.core.conditions

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class PlatformConditionTest {

    private lateinit var classUnderTest: PlatformCondition

    @BeforeEach
    internal fun setUp() {
        classUnderTest = PlatformCondition()
    }

    @Test
    @DisplayName("Given parameter is iOS, When checkWith is called, Then it should return false")
    fun checkWithShouldReturnFalse() {
        val result = classUnderTest.checkWith(PlatformCondition.PlatformData("iOS"))
        Assertions.assertThat(result).isFalse
    }

    @Test
    @DisplayName("Given parameter is iOS, When checkWithFlow is called, Then it should return false")
    fun checkWithFlowShouldReturnFalse() {
        runTest {
            val result = classUnderTest.checkWithFlow(
                PlatformCondition.PlatformData("iOS")
            ).first()
            Assertions.assertThat(result).isFalse
        }
    }

    @Test
    @DisplayName("Given parameter is android, When checkWith is called, Then it should return true")
    fun checkWithShouldReturnTrue() {
        val result = classUnderTest.checkWith(PlatformCondition.PlatformData("android"))
        Assertions.assertThat(result).isTrue
    }

    @Test
    @DisplayName("Given parameter is android, When checkWithFlow is called, Then it should return true")
    fun checkWithFlowShouldReturnTrue() {
        runTest {
            val result = classUnderTest.checkWithFlow(
                PlatformCondition.PlatformData("android")
            ).first()
            Assertions.assertThat(result).isTrue
        }
    }

    @Test
    @DisplayName("Given parameter is Android, When checkWith is called, Then it should return true")
    fun checkWithUpperCaseShouldReturnTrue() {
        val result = classUnderTest.checkWith(PlatformCondition.PlatformData("Android"))
        Assertions.assertThat(result).isTrue
    }

    @Test
    @DisplayName("Given parameter is Android, When checkWithFlow is called, Then it should return true")
    fun checkWithFlowUpperCaseShouldReturnTrue() {
        runTest {
            val result = classUnderTest.checkWithFlow(
                PlatformCondition.PlatformData("Android")
            ).first()
            Assertions.assertThat(result).isTrue
        }
    }
}