package com.greencopper.maps.common

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.mapsmocks.MockMapsRepository
import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class MyLocationsManagerTest {
    private lateinit var classUnderTest: MyLocationsManager
    private lateinit var remoteStateDispatcher: MockRemoteStateDispatcher

    private val lazyLocalStorage: LazyResolver<LocalStorage> =
        LazyResolver.adhoc(LocalStorage("project"))

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        remoteStateDispatcher = MockRemoteStateDispatcher(json = App.resolve())
        classUnderTest = MyLocationsManager(
            remoteStateDispatcher,
            lazyLocalStorage,
            MockMapsRepository()
        )
    }

    @Test
    @DisplayName("Given there is no favorites added, When calling favoriteIds, Then it should be empty")
    fun favoriteIdsShouldBeEmpty() {
        assertThat(classUnderTest.favoriteIds).isEmpty()
    }

    @Test
    @DisplayName("Given there is no favorites added, When calling favoriteIdsFlow, Then it should be empty")
    fun favoriteIdsFlowShouldBeEmpty() {
        runTest {
            assertThat(classUnderTest.favoriteIdsFlow.first()).isEmpty()
        }
    }

    @Test
    @DisplayName("Given there is no favorites, When we add two, Then it should return two favorites")
    fun addToFavoritesShouldAddTwo() {
        classUnderTest.addToFavorites(Location("1"))
        classUnderTest.addToFavorites(Location("2"))

        assertThat(classUnderTest.favoriteIds).hasSize(2)
        runTest {
            assertThat(classUnderTest.favoriteIdsFlow.first()).hasSize(2)
        }
        assertThat(remoteStateDispatcher.dispatchCallCount).isEqualTo(2)
    }

    @Test
    @DisplayName("Given a favorite, When we add it again, Then it should do nothing")
    fun addToFavoritesShouldDoNothing() {
        classUnderTest.addToFavorites(Location("1"))
        classUnderTest.addToFavorites(Location("1"))

        assertThat(classUnderTest.favoriteIds).hasSize(1)
        runTest {
            assertThat(classUnderTest.favoriteIdsFlow.first()).hasSize(1)
        }
        assertThat(remoteStateDispatcher.dispatchCallCount).isEqualTo(1)
    }

    @Test
    @DisplayName("Given there is no favorite, When we remove one, Then it should return zero favorites")
    fun removeFromFavoritesShouldRemoveZero() {
        classUnderTest.removeFromFavorites(Location("1"))

        assertThat(classUnderTest.favoriteIds).isEmpty()
        runTest {
            assertThat(classUnderTest.favoriteIdsFlow.first()).isEmpty()
        }
        assertThat(remoteStateDispatcher.dispatchCallCount).isEqualTo(0)
    }

    @Test
    @DisplayName("Given there are three favorites, When we remove one, Then it should return two favorites")
    fun removeFromFavoritesShouldRemoveTwo() {
        lazyLocalStorage.resolve().project.maps.myLocations.value = setOf("1", "2", "3")

        classUnderTest.removeFromFavorites(Location("1"))
        assertThat(classUnderTest.favoriteIds).hasSize(2)
        runTest {
            assertThat(classUnderTest.favoriteIdsFlow.first()).hasSize(2)
        }

        classUnderTest.removeFromFavorites(Location("4"))
        assertThat(classUnderTest.favoriteIds).hasSize(2)
        runTest {
            assertThat(classUnderTest.favoriteIdsFlow.first()).hasSize(2)
        }
        assertThat(remoteStateDispatcher.dispatchCallCount).isEqualTo(1)
    }

    data class Location(override val itemId: String) : Favoriteable<String>
}
