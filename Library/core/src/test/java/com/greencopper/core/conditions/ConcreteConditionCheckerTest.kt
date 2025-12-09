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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConcreteConditionCheckerTest {

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
    fun whenCheckingConditionSet_asTrue_shouldBeTrue() {
        val conditionSet = ConditionSet(
            predicate = "predicate1 AND predicate2",
            conditions = mapOf(
                "predicate1" to conditionBuilder(value = true),
                "predicate2" to conditionBuilder(value = true)
            )
        )
        val validity = conditionChecker.check(conditionSet)
        assertThat(validity).isTrue
    }

    @Test
    fun whenCheckingConditionSet_asFalse_shouldBeFalse() {
        val conditionSet = ConditionSet(
            predicate = "predicate1 AND predicate2",
            conditions = mapOf(
                "predicate1" to conditionBuilder(value = true),
                "predicate2" to conditionBuilder(value = false)
            )
        )
        val validity = conditionChecker.check(conditionSet)
        assertThat(validity).isFalse
    }

    @Test
    fun whenCheckingConditionSetWithUnknownKey_shouldBeFallback() {
        val conditionSet = ConditionSet(
            predicate = "predicate1",
            mapOf(
                "predicate1" to conditionBuilder(
                    key = ConditionInfo.Key("Null", -1),
                    value = true,
                    fallback = false
                )
            )
        )
        val validity = conditionChecker.check(conditionSet)
        assertThat(validity).isFalse
    }

    @Test
    fun whenCheckingConditionSetWithFailingCheck_shouldBeFallback() {
        val conditionSet = ConditionSet(
            predicate = "predicate1",
            mapOf(
                "predicate1" to conditionBuilder(
                    valueKey = "unknown",
                    value = true,
                    fallback = false
                )
            )
        )
        val validity = conditionChecker.check(conditionSet)
        assertThat(validity).isFalse
    }

    @Test
    fun whenCheckingConditionInfoTrue_withCondition_shouldBeTrue() {
        val condition = conditionBuilder(value = true)
        val validity = conditionChecker.check(condition)
        assertThat(validity).isTrue
    }

    @Test
    fun whenCheckingConditionInfoFalse_withCondition_shouldBeFalse() {
        val condition = conditionBuilder(value = false)
        val validity = conditionChecker.check(condition)
        assertThat(validity).isFalse
    }

    @Test
    fun whenCheckingConditionInfo_withoutCondition_shouldBeFallback() {
        val condition =
            conditionBuilder(key = ConditionInfo.Key("unknown", 1), value = false, fallback = true)
        val validity = conditionChecker.check(condition)
        assertThat(validity).isTrue
    }

    @Test
    fun whenCheckingFlowConditionSet_asTrue_shouldBeTrue() {
        val conditionSet = ConditionSet(
            predicate = "predicate1 AND predicate2", mapOf(
                "predicate1" to conditionBuilder(value = true),
                "predicate2" to conditionBuilder(value = true)
            )
        )
        runTest {
            val validity = conditionChecker.checkFlow(conditionSet).first()
            assertThat(validity).isTrue
        }
    }

    @Test
    fun whenCheckingFlowConditionSet_asFalse_shouldBeFalse() {
        val conditionSet = ConditionSet(
            predicate = "predicate1 AND predicate2",
            mapOf(
                "predicate1" to conditionBuilder(value = true),
                "predicate2" to conditionBuilder(value = false)
            )
        )
        runTest {
            val validity = conditionChecker.checkFlow(conditionSet).first()
            assertThat(validity).isFalse
        }
    }

    @Test
    fun whenCheckingFlowConditionSetWithUnknownKey_shouldBeFallback() {
        val conditionSet = ConditionSet(
            predicate = "predicate1",
            mapOf(
                "predicate1" to conditionBuilder(
                    key = ConditionInfo.Key("Null", -1),
                    value = true,
                    fallback = false
                )
            )
        )
        runTest {
            val validity = conditionChecker.checkFlow(conditionSet).first()
            assertThat(validity).isFalse
        }
    }

    @Test
    fun whenCheckingFlowConditionSetWithFailingCheck_shouldBeFallback() {
        val conditionSet = ConditionSet(
            predicate = "predicate1",
            mapOf(
                "predicate1" to conditionBuilder(
                    valueKey = "unknown",
                    value = true,
                    fallback = false
                )
            )
        )
        runTest {
            val validity = conditionChecker.checkFlow(conditionSet).first()
            assertThat(validity).isFalse
        }
    }

    @Test
    fun whenCheckingFlowConditionInfoTrue_withCondition_shouldBeTrue() {
        val condition = conditionBuilder(value = true)
        runTest {
            val validity = conditionChecker.checkFlow(condition).first()
            assertThat(validity).isTrue
        }
    }

    @Test
    fun whenCheckingFlowConditionInfoFalse_withCondition_shouldBeFalse() {
        val condition = conditionBuilder(value = false)
        runTest {
            val validity = conditionChecker.checkFlow(condition).first()
            assertThat(validity).isFalse
        }
    }

    @Test
    fun whenCheckingFlowConditionInfo_withoutCondition_shouldBeFallback() {
        val condition =
            conditionBuilder(key = ConditionInfo.Key("unknown", 1), value = false, fallback = true)
        runTest {
            val validity = conditionChecker.checkFlow(condition).first()
            assertThat(validity).isTrue
        }
    }

    @Test
    fun whenCheckingFlowConditionInfo_withChangingCondition_shouldBeChanging() {
        localStorage.app.core.conditionTest.test.value = false
        val condition = ConditionInfo(TestUpdatingCondition.key, null, false)

        runTest {
            var validity = conditionChecker.checkFlow(condition).first()
            assertThat(validity).isFalse
            localStorage.app.core.conditionTest.test.value = true
            validity = conditionChecker.checkFlow(condition).first()
            assertThat(validity).isTrue
        }
    }
}

internal fun conditionBuilder(
    key: ConditionInfo.Key = TestCondition.key,
    valueKey: String = "value",
    value: Boolean,
    fallback: Boolean = false
): ConditionInfo {
    return ConditionInfo(
        key,
        params = buildJsonObject { put(valueKey, value) },
        fallback = fallback
    )
}
