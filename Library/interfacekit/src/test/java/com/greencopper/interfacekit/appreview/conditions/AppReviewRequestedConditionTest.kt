package com.greencopper.interfacekit.appreview.conditions

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.appreview.localstorage.AppReviewRequest
import com.greencopper.interfacekit.appreview.localstorage.appReviewRequests
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

internal class CanRequestAppReviewConditionTest {

    private lateinit var condition: CanRequestAppReviewCondition
    private lateinit var localStorage: LocalStorage
    private lateinit var buildConfigProvider: MockBuildConfigProvider

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        localStorage = App.resolve()

        buildConfigProvider = MockBuildConfigProvider(
            mockVersionName = "123"
        )

        val now = Instant.now()
        localStorage.app.interfaceKit.appReviewRequests.requests.value = listOf(
            AppReviewRequest("120", now.minusSeconds(1500)),
            AppReviewRequest("121", now.minusSeconds(1000)),
            AppReviewRequest("122", now.minusSeconds(500)),
        )

        condition = CanRequestAppReviewCondition(localStorage, buildConfigProvider)
    }

    @Test
    fun checkWithNoRegisteredRequests_shouldReturnTrue() {
        localStorage.app.interfaceKit.appReviewRequests.requests.value = emptyList()
        val param = CanRequestAppReviewCondition.AppReviewRequestData()

        assertThat(condition.checkWith(param)).isTrue
    }

    @Test
    fun checkWithEmptyParam_shouldReturnTrue() {
        val param = CanRequestAppReviewCondition.AppReviewRequestData()
        assertThat(condition.checkWith(param)).isTrue
    }

    @Test
    fun checkWithVersion_differentVersion_shouldReturnTrue() {
        val param = CanRequestAppReviewCondition.AppReviewRequestData(true)
        assertThat(condition.checkWith(param)).isTrue
    }

    @Test
    fun checkWithVersion_sameVersion_shouldReturnFalse() {
        buildConfigProvider.mockVersionName = "122"
        val param = CanRequestAppReviewCondition.AppReviewRequestData(true)
        assertThat(condition.checkWith(param)).isFalse
    }

    @Test
    fun checkWithInterval_aboveThreshold_shouldReturnTrue() {
        val param = CanRequestAppReviewCondition.AppReviewRequestData(intervalSincePreviousRequest = 100)
        assertThat(condition.checkWith(param)).isTrue
    }

    @Test
    fun checkWithInterval_underThreshold_shouldReturnFalse() {
        val param = CanRequestAppReviewCondition.AppReviewRequestData(intervalSincePreviousRequest = 700)
        assertThat(condition.checkWith(param)).isFalse
    }

    @Test
    fun checkWithParams_shouldReturnTrue() {
        val param = CanRequestAppReviewCondition.AppReviewRequestData(true, 100)
        assertThat(condition.checkWith(param)).isTrue
    }

    @Test
    fun checkWithFlowParams_shouldReturnTrue() {
        val param = CanRequestAppReviewCondition.AppReviewRequestData(true, 100)

        runTest {
            val result = condition.checkWithFlow(param).first()
            assertThat(result).isTrue
        }
    }

}
