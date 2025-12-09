package com.greencopper.interfacekit.onboarding.pages

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.onboarding.onboarding
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageCompletionCondition.OnboardingPageCompletionConditionData
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class OnboardingPageCompletionConditionTest {

    private lateinit var classUnderTest: OnboardingPageCompletionCondition
    private lateinit var lazyLocalStorage: LazyResolver<LocalStorage>

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
        lazyLocalStorage = LazyResolver.adhoc(LocalStorage("project"))
        classUnderTest =
            OnboardingPageCompletionCondition(lazyLocalStorage)
    }

    @Nested
    @DisplayName("Given valid parameters with page completed")
    inner class IsCompleted {
        private val data = OnboardingPageCompletionConditionData(
            pageId = "id",
            completed = true
        )

        @Test
        @DisplayName("When calling checkWith, Then true should be returned")
        fun checkWithShouldReturnTrue() {
            lazyLocalStorage.resolve().project.interfaceKit.onboarding.completedPages.value =
                setOf("id")
            val result = classUnderTest.checkWith(data)
            assertThat(result).isTrue
        }

        @Test
        @DisplayName("When calling checkWith, Then false should be returned")
        fun checkWithShouldReturnFalse() {
            lazyLocalStorage.resolve().project.interfaceKit.onboarding.completedPages.value =
                emptySet()
            val result = classUnderTest.checkWith(data)
            assertThat(result).isFalse
        }

        @Test
        @DisplayName("When calling checkWithFlow, Then true should be returned")
        fun checkWithFlowShouldReturnTrue() {
            lazyLocalStorage.resolve().project.interfaceKit.onboarding.completedPages.value =
                setOf("id")
            val result = classUnderTest.checkWithFlow(data)
            runTest {
                assertThat(result.first()).isTrue
            }
        }

        @Test
        @DisplayName("When calling checkWithFlow, Then false should be returned")
        fun checkWithFlowShouldReturnFalse() {
            lazyLocalStorage.resolve().project.interfaceKit.onboarding.completedPages.value =
                emptySet()
            val result = classUnderTest.checkWithFlow(data)
            runTest {
                assertThat(result.first()).isFalse
            }
        }

        @Test
        @DisplayName("When calling deserialize, Then it should return the data")
        fun serializeShouldSucceed() {
            Toolkit.setupTest()

            val encodedData = data.encodeToJsonElement()
            assertThat(classUnderTest.deserialize(encodedData)).isInstanceOf(
                OnboardingPageCompletionConditionData::class.java
            )
        }
    }

    @Nested
    @DisplayName("Given valid parameters with page not completed")
    inner class IsNotCompleted {
        private val data = OnboardingPageCompletionConditionData(
            pageId = "id",
            completed = false
        )

        @Test
        @DisplayName("When calling checkWith, Then false should be returned")
        fun checkWithShouldReturnTrue() {
            lazyLocalStorage.resolve().project.interfaceKit.onboarding.completedPages.value =
                setOf("id")
            val result = classUnderTest.checkWith(data)
            assertThat(result).isFalse
        }

        @Test
        @DisplayName("When calling checkWith, Then true should be returned")
        fun checkWithShouldReturnFalse() {
            lazyLocalStorage.resolve().project.interfaceKit.onboarding.completedPages.value =
                emptySet()
            val result = classUnderTest.checkWith(data)
            assertThat(result).isTrue
        }

        @Test
        @DisplayName("When calling checkWithFlow, Then false should be returned")
        fun checkWithFlowShouldReturnTrue() {
            lazyLocalStorage.resolve().project.interfaceKit.onboarding.completedPages.value =
                setOf("id")
            val result = classUnderTest.checkWithFlow(data)
            runTest {
                assertThat(result.first()).isFalse
            }
        }

        @Test
        @DisplayName("When calling checkWithFlow, Then true should be returned")
        fun checkWithFlowShouldReturnFalse() {
            lazyLocalStorage.resolve().project.interfaceKit.onboarding.completedPages.value =
                emptySet()
            val result = classUnderTest.checkWithFlow(data)
            runTest {
                assertThat(result.first()).isTrue
            }
        }

        @Test
        @DisplayName("When calling deserialize, Then it should return the data")
        fun serializeShouldSucceed() {
            Toolkit.setupTest()

            val encodedData = data.encodeToJsonElement()
            assertThat(classUnderTest.deserialize(encodedData)).isInstanceOf(
                OnboardingPageCompletionConditionData::class.java
            )
        }
    }

    @Test
    @DisplayName("Given invalid data, When calling deserialize, Then it should throw")
    fun serializeShouldFail() {
        Toolkit.setupTest()

        val encodedData = JsonNull
        assertThrows<SerializationException> {
            classUnderTest.deserialize(encodedData)
        }
    }
}