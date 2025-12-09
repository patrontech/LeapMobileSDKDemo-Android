package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.ConcreteConditionChecker
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.conditions.parser.ComplexPredicateParser
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConditionedTest {

    private val conditionChecker: ConditionChecker
    private val localStorage: LocalStorage

    init {
        Toolkit.setupTest()
        localStorage = App.resolve()
        conditionChecker = ConcreteConditionChecker(
            ComplexPredicateParser(MockBuildConfigProvider()),
            MapConditionResolver.createTestResolver(localStorage),
            App.resolve(),
        )
    }

    @Test
    internal fun filterConditioned() {
        val conditionInfo1 = conditionBuilder(value = false, fallback = true)
        val conditionSet1 = ConditionSet("condition1", mapOf("condition1" to conditionInfo1))
        val conditionInfo2 = conditionBuilder(value = true, fallback = true)
        val conditionSet2 = ConditionSet("condition2", mapOf("condition2" to conditionInfo2))
        val conditioned1 = MockConditioned(1, conditionSet1)
        val conditioned2 = MockConditioned(2, conditionSet2)
        val conditioned3 = MockConditioned(3, null)
        val conditioned = listOf(conditioned1, conditioned2, conditioned3)
        val authorized = conditioned.authorized(conditionChecker)

        assertThat(authorized).hasSize(2)
        assertThat(authorized).containsExactly(conditioned2, conditioned3)
    }

    @Test
    internal fun filterConditionedFlow() {
        localStorage.app.core.conditionTest.test.value = false
        val condition = ConditionInfo(TestUpdatingCondition.key, null, false)
        val conditionSet = ConditionSet("condition1", mapOf("condition1" to condition))
        val conditioned = MockConditioned(1, conditionSet)
        val conditionedList = listOf(conditioned)

        runTest {
            var authorized = conditionedList.authorizedFlow(conditionChecker).first()
            assertThat(authorized).hasSize(0)

            localStorage.app.core.conditionTest.test.value = true

            authorized = conditionedList.authorizedFlow(conditionChecker).first()
            assertThat(authorized).hasSize(1)
        }
    }
}

private data class MockConditioned(val id: Int, override val conditionSet: ConditionSet?) : Conditioned
