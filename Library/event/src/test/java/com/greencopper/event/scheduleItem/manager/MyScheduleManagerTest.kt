package com.greencopper.event.scheduleItem.manager

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.event.common.event
import com.greencopper.event.scheduleItem.MyScheduleManager
import com.greencopper.eventmocks.MockReminderUIManager
import com.greencopper.eventmocks.MockScheduleItemRepository
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.interfacekit.MockFavoriteable
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MyScheduleManagerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val lazyLocalStorage: LazyResolver<LocalStorage> = LazyResolver.adhoc(LocalStorage("project"))
    private val reminderUIManager = MockReminderUIManager()
    private val manager = MyScheduleManager(
        MockScheduleItemRepository(),
        MockRemoteStateDispatcher(json = App.resolve()),
        lazyLocalStorage,
        reminderUIManager,
    )

    override fun afterEach() {}

    @Test
    fun onInit_scheduleItems_isEmpty() {
        assertThat(manager.favoriteIds).isEmpty()
        runTest {
            assertThat(manager.favoriteIdsFlow.first()).isEmpty()
        }
    }

    @Test
    fun addTest() {
        manager.addToFavorites(MockFavoriteable(1L))
        manager.addToFavorites(MockFavoriteable(2L))

        assertThat(manager.favoriteIds).hasSize(2)
        runTest {
            assertThat(manager.favoriteIdsFlow.first()).hasSize(2)
        }
    }

    @Test
    fun withNothingToRemove_removeDoesNotFail() {
        manager.removeFromFavorites(MockFavoriteable(1L))

        assertThat(manager.favoriteIds).isEmpty()
        runTest {
            assertThat(manager.favoriteIdsFlow.first()).isEmpty()
        }
    }

    @Test
    fun removeTest() {
        lazyLocalStorage.resolve().project.event.myScheduleItemIds.value = setOf(1L, 2L, 3L)

        manager.removeFromFavorites(MockFavoriteable(1L))
        assertThat(manager.favoriteIds).hasSize(2)
        runTest {
            assertThat(manager.favoriteIdsFlow.first()).hasSize(2)
        }

        manager.removeFromFavorites(MockFavoriteable(4L))
        assertThat(manager.favoriteIds).hasSize(2)
        runTest {
            assertThat(manager.favoriteIdsFlow.first()).hasSize(2)
        }
    }
}
