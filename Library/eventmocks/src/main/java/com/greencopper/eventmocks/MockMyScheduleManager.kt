package com.greencopper.eventmocks

import com.greencopper.event.reminders.ui.ReminderUIManager
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.lists.ListRepository
import com.greencopper.testmocks.interfacekit.MockListRepository
import kotlinx.coroutines.flow.*

public class MockMyScheduleManager(
    initialItems: Set<Long>,
    private val reminderUIManager: ReminderUIManager = MockReminderUIManager(),
    override val repository: ListRepository<*> = MockListRepository()
) : FavoritesManager<Long> {

    override val favoriteIds: Set<Long>
        get() = mutableFavorites.value

    public val mutableFavorites: MutableStateFlow<Set<Long>> = MutableStateFlow(initialItems)
    override val favoriteIdsFlow: Flow<Set<Long>> = mutableFavorites

    override fun addToFavorites(item: Favoriteable<Long>) {
        mutableFavorites.update { it + item.itemId }
        reminderUIManager.onAddToMySchedule()
    }

    override fun removeFromFavorites(item: Favoriteable<Long>) {
        mutableFavorites.update { it - item.itemId }
    }

    override fun isInFavorites(item: Favoriteable<Long>): Boolean =
        favoriteIds.contains(item.itemId)

    override fun isInFavorites(itemId: Long): Boolean =
        favoriteIds.contains(itemId)
}
