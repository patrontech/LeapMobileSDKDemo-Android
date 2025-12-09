package com.greencopper.interfacekit.onboarding.pages

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.onboarding.onboarding
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

internal class SinceLastPageCompletionConditionTest {

    init {
        Toolkit.setupTest()
    }

    private val localStorage = LocalStorage("project")
    private val lazyLocalStorage = LazyResolver.adhoc(localStorage)
    private val json = App.resolve<Json>()

    private val params = SinceLastPageCompletionCondition.SinceLastPageCompletionConditionData(
        "pageId",
        1000
    )

    private val condition = SinceLastPageCompletionCondition(lazyLocalStorage)

    @Test
    fun givenNoCompletions_checkWith_returnsTrue() {
        assertThat(condition.checkWith(params)).isTrue
    }

    @Test
    fun givenNoCompletions_checkWithFlow_returnsTrue() {
        runTest {
            assertThat(condition.checkWithFlow(params).first()).isTrue
        }
    }

    @Test
    fun givenRecentCompletion_checkWith_returnsFalse() {
        localStorage.project.interfaceKit.onboarding.lastOnboardingPageCompletions.value = mapOf(
            params.pageId to Instant.now().epochSecond
        )

        assertThat(condition.checkWith(params)).isFalse
    }

    @Test
    fun givenRecentCompletion_checkWithFlow_returnsFalse() {
        localStorage.project.interfaceKit.onboarding.lastOnboardingPageCompletions.value = mapOf(
            params.pageId to Instant.now().epochSecond
        )

        runTest {
            assertThat(condition.checkWithFlow(params).first()).isFalse
        }
    }

    @Test
    fun givenCompletionInThePast_checkWith_returnsTrue() {
        localStorage.project.interfaceKit.onboarding.lastOnboardingPageCompletions.value = mapOf(
            params.pageId to (Instant.now().epochSecond - (params.atLeastSince * 2))
        )

        assertThat(condition.checkWith(params)).isTrue
    }

    @Test
    fun givenCompletionInThePast_checkWithFlow_returnsTrue() {
        localStorage.project.interfaceKit.onboarding.lastOnboardingPageCompletions.value = mapOf(
            params.pageId to (Instant.now().epochSecond - (params.atLeastSince * 2))
        )

        runTest {
            assertThat(condition.checkWithFlow(params).first()).isTrue
        }
    }

    @Test
    fun givenValidData_deserialize_returnsData() {
        val params = SinceLastPageCompletionCondition.SinceLastPageCompletionConditionData("pageId", 1L)
        val result = condition.deserialize(json.encodeToJsonElement(params))

        assertThat(result).isEqualTo(params)
    }

    @Test
    fun givenInvalidData_deserialize_throws() {
        assertThrows<Exception> {
            condition.deserialize(JsonNull)
        }
    }
}
