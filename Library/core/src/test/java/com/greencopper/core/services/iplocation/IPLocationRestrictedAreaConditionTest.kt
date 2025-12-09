package com.greencopper.core.services.iplocation

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockIPLocationService
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal typealias ConditionData = IPLocationRestrictedAreaCondition.IPLocationRestrictedAreaConditionData

internal class IPLocationRestrictedAreaConditionTest: CoroutineTest(UnconfinedTestDispatcher()) {

    private val localStorage = LocalStorage("project")
    private val condition = IPLocationRestrictedAreaCondition( localStorage, MockIPLocationService())

    init {
       Toolkit.setupTest()
        localStorage.app.core.iplocation.value = IPLocation("NA", "CA", RestrictedArea.OUTSIDE_RESTRICTED_AREA)
    }

    override fun afterEach() {}

    @Test
    fun checkWithShouldWaitForIPLocationServiceCompletedFlow() {
        val parameter = ConditionData(RestrictedArea.IN_RESTRICTED_AREA)
        localStorage.app.core.iplocation.value = null
        val result = condition.checkWith(parameter)
        assertThat(result).isTrue
    }

    @Test
    fun checkWithShouldReturnFalse() {
        val parameter = ConditionData(RestrictedArea.IN_RESTRICTED_AREA)
        val result = condition.checkWith(parameter)
        assertThat(result).isFalse
    }

    @Test
    fun checkWithFlowShouldReturnFalse() =
        runTest {
            val parameter = ConditionData(RestrictedArea.IN_RESTRICTED_AREA)
            val result = condition
                .checkWithFlow(parameter)
                .first()
            assertThat(result).isFalse
        }

    @Test
    fun checkWithShouldReturnTrue() {
        val parameter = ConditionData(RestrictedArea.OUTSIDE_RESTRICTED_AREA)
        val result = condition.checkWith(parameter)
        assertThat(result).isTrue
    }

    @Test
    fun checkWithFlowShouldReturnTrue() =
        runTest {
            val parameter = ConditionData(RestrictedArea.OUTSIDE_RESTRICTED_AREA)
            val result = condition
                .checkWithFlow(parameter)
                .first()
            assertThat(result).isTrue
        }
}
