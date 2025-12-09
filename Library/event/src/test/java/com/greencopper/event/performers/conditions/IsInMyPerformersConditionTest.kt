package com.greencopper.event.performers.conditions

import com.greencopper.testmocks.interfacekit.MockFavoriteable
import com.greencopper.testmocks.interfacekit.MockFavoritesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class IsInMyPerformersConditionTest {
    private val myPerformersItemIds = setOf("1", "2", "3")
    private val myPerformersManager = MockFavoritesManager<String>().apply {
        myPerformersItemIds.forEach {
            addToFavorites(MockFavoriteable(it))
        }
    }
    private val condition = IsInMyPerformersCondition(myPerformersManager)

    @Test
    @DisplayName("Given wrong item id, When checkWith is called, Then it should return false")
    fun checkWithShouldReturnFalse() {
        val result = condition.checkWith(IsInMyPerformersCondition.MyPerformersData("4"))
        assertThat(result).isFalse
    }

    @Test
    @DisplayName("Given wrong item id, When checkWithFlow is called, Then it should return false")
    fun checkWithFlowShouldReturnFalse() {
        runTest {
            val result = condition
                .checkWithFlow(IsInMyPerformersCondition.MyPerformersData("4"))
                .first()
            assertThat(result).isFalse
        }
    }

    @Test
    @DisplayName("Given correct item id, When checkWith is called, Then it should return true")
    fun checkWithShouldReturnTrue() {
        val result = condition.checkWith(IsInMyPerformersCondition.MyPerformersData("1"))
        assertThat(result).isTrue
    }

    @Test
    @DisplayName("Given correct item id, When checkWithFlow is called, Then it should return true")
    fun checkWithFlowShouldReturnTrue() {
        runTest {
            val result = condition
                .checkWithFlow(IsInMyPerformersCondition.MyPerformersData("2"))
                .first()
            assertThat(result).isTrue
        }
    }
}
