package com.greencopper.maps.common

import com.greencopper.testmocks.interfacekit.MockFavoriteable
import com.greencopper.testmocks.interfacekit.MockFavoritesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class IsInMyLocationsConditionTest {
    private val myLocationsItemIds = setOf("1", "2", "3")
    private val myLocationsManager = MockFavoritesManager<String>().apply {
        myLocationsItemIds.forEach {
            addToFavorites(MockFavoriteable(it))
        }
    }
    private val condition = IsInMyLocationsCondition(myLocationsManager)

    @Test
    @DisplayName("Given wrong item id, When checkWith is called, Then it should return false")
    fun checkWithShouldReturnFalse() {
        val result = condition.checkWith(IsInMyLocationsCondition.MyLocationData("4"))
        assertThat(result).isFalse
    }

    @Test
    @DisplayName("Given wrong item id, When checkWithFlow is called, Then it should return false")
    fun checkWithFlowShouldReturnFalse() {
        runTest {
            val result = condition
                .checkWithFlow(IsInMyLocationsCondition.MyLocationData("4"))
                .first()
            assertThat(result).isFalse
        }
    }

    @Test
    @DisplayName("Given correct item id, When checkWith is called, Then it should return true")
    fun checkWithShouldReturnTrue() {
        val result = condition.checkWith(IsInMyLocationsCondition.MyLocationData("1"))
        assertThat(result).isTrue
    }

    @Test
    @DisplayName("Given correct item id, When checkWithFlow is called, Then it should return true")
    fun checkWithFlowShouldReturnTrue() {
        runTest {
            val result = condition
                .checkWithFlow(IsInMyLocationsCondition.MyLocationData("2"))
                .first()
            assertThat(result).isTrue
        }
    }
}
