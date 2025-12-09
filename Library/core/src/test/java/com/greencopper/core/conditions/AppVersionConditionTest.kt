package com.greencopper.core.conditions

import com.greencopper.core.conditions.AppVersionCondition.AppVersionData.Exact
import com.greencopper.core.conditions.AppVersionCondition.AppVersionData.OlderThan
import com.greencopper.core.data.KiboSerializable
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AppVersionConditionTest {

    init {
        Toolkit.setupTest()
    }

    @Nested
    @DisplayName("Using Exact condition")
    inner class ExactCondition {

        private val buildConfigProvider = MockBuildConfigProvider(
            mockVersionName = "1.2.3",
            mockVersionCode = 1
        )
        private val classUnderTest = AppVersionCondition(buildConfigProvider)

        @Test
        @DisplayName("Given list of the wrong versions, When checkWith is called, Then it should return false")
        fun checkWithShouldReturnFalse() {
            val result = classUnderTest.checkWith(Exact(listOf("2", "3", "1.4"), true))
            assertThat(result).isFalse
        }

        @Test
        @DisplayName("Given list of the wrong versions, When checkWithFlow is called, Then it should return false")
        fun checkWithFlowShouldReturnFalse() {
            runTest {
                val result = classUnderTest
                    .checkWithFlow(Exact(listOf("2", "3", "1.4"), true))
                    .first()
                assertThat(result).isFalse
            }
        }

        @Test
        @DisplayName("Given list with correct version, When checkWith is called with versionCode, Then it should return true")
        fun checkWithVersionCodeShouldReturnTrue() {
            val result = classUnderTest.checkWith(Exact(listOf("1", "2"), true))
            assertThat(result).isTrue
        }

        @Test
        @DisplayName("Given list with correct version, When checkWith is called with versionName, Then it should return true")
        fun checkWithVersionNameShouldReturnTrue() {
            val result = classUnderTest.checkWith(Exact(listOf("2", "3", "1.2.3"), true))
            assertThat(result).isTrue
        }

        @Test
        @DisplayName("Given list with correct version, When checkWithFlow is called, Then it should return true")
        fun checkWithFlowShouldReturnTrue() {
            runTest {
                val result = classUnderTest
                    .checkWithFlow(Exact(listOf("1", "2"), true))
                    .first()
                assertThat(result).isTrue
            }
        }

        @Test
        @DisplayName("Given empty list, When checkWith is called, Then it should return false")
        fun checkWithEmptyListShouldReturnFalse() {
            val result = classUnderTest.checkWith(Exact(emptyList(), true))
            assertThat(result).isFalse
        }

        @Test
        fun givenMatchingVersionsAndNotInVersions_checkWith_returnsFalse() {
            val result = classUnderTest.checkWith(Exact(listOf("1", "2"), false))
            assertThat(result).isFalse
        }

        @Test
        fun givenNoMatchingVersionAndNotInVersions_checkWith_returnsTrue() {
            val result = classUnderTest.checkWith(Exact(listOf("2", "3"), false))
            assertThat(result).isTrue
        }
    }

    @Nested
    @DisplayName("Using Exact condition")
    inner class OlderThanCondition {

        private val buildConfigProvider = MockBuildConfigProvider(
            mockVersionName = "10.5.5",
            mockVersionCode = 10
        )
        private val classUnderTest = AppVersionCondition(buildConfigProvider)

        @Test
        @DisplayName("Given version older than, When checkWith is called, Then it should return true")
        fun checkWithOlderShouldReturnTrue() {
            classUnderTest.checkWith(OlderThan("11")) shouldBe true
            classUnderTest.checkWith(OlderThan("10.6")) shouldBe true
            classUnderTest.checkWith(OlderThan("10.5.5.5")) shouldBe true
        }

        @Test
        @DisplayName("Given version newer than, When checkWith is called, Then it should return false")
        fun checkWithNewerShouldReturnFalse() {
            classUnderTest.checkWith(OlderThan("9")) shouldBe false
            classUnderTest.checkWith(OlderThan("10.4")) shouldBe false
            classUnderTest.checkWith(OlderThan("10.5.4")) shouldBe false
        }

        @Test
        @DisplayName("Given version same as, When checkWith is called, Then it should return false")
        fun checkWithSameShouldReturnFalse() {
            classUnderTest.checkWith(OlderThan("10.5.5")) shouldBe false
            classUnderTest.checkWith(OlderThan("10.5.5.0")) shouldBe false
        }
    }

    @Test
    fun testSerializer() {
        testKiboSerializable(Exact(listOf("1", "2"), true))
        testKiboSerializable(OlderThan("1.3.2"))

        val jsonText = """{
                    "versions": ["1", "2"],
                    "isInVersions": "true",
                    "olderThan": "1.3.2"
                }"""
        val decodeFromString = KiboSerializable.decodeFromString<Exact>(jsonText)
        decodeFromString shouldBe Exact(listOf("1", "2"), true)

        val jsonTextFailing = """{
                      "test": true
                    }"""
        assertThrows<SerializationException> {
            KiboSerializable.decodeFromString<Exact>(jsonTextFailing)
        }
    }
}
