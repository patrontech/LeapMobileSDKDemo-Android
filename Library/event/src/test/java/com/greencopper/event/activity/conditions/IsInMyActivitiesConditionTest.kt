package com.greencopper.event.activity.conditions

import com.greencopper.testmocks.interfacekit.MockFavoriteable
import com.greencopper.testmocks.interfacekit.MockFavoritesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class IsInMyActivitiesConditionTest {

    private val myActivitiesItemIds = setOf(1L, 2L, 3L)
    private val myActivitiesManager = MockFavoritesManager<Long>().apply {
        myActivitiesItemIds.forEach {
            addToFavorites(MockFavoriteable(it))
        }
    }
    private val condition = IsInMyActivitiesCondition(myActivitiesManager)

    @Test
    @DisplayName("Given wrong item id, When checkWith is called, Then it should return false")
    fun checkWithShouldReturnFalse() {
        val result = condition.checkWith(IsInMyActivitiesCondition.MyActivityData(4))
        assertThat(result).isFalse
    }

    @Test
    @DisplayName("Given wrong item id, When checkWithFlow is called, Then it should return false")
    fun checkWithFlowShouldReturnFalse() {
        runTest {
            val result = condition
                .checkWithFlow(IsInMyActivitiesCondition.MyActivityData(4))
                .first()
            assertThat(result).isFalse
        }
    }

    @Test
    @DisplayName("Given correct item id, When checkWith is called, Then it should return true")
    fun checkWithShouldReturnTrue() {
        val result = condition.checkWith(IsInMyActivitiesCondition.MyActivityData(1))
        assertThat(result).isTrue
    }

    @Test
    @DisplayName("Given correct item id, When checkWithFlow is called, Then it should return true")
    fun checkWithFlowShouldReturnTrue() {
        runTest {
            val result = condition
                .checkWithFlow(IsInMyActivitiesCondition.MyActivityData(2))
                .first()
            assertThat(result).isTrue
        }
    }
}
