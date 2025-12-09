package com.greencopper.event.scheduleItem

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.event.common.event
import com.greencopper.event.reminders.ui.ReminderUIManager
import com.greencopper.event.scheduleItem.data.repository.ScheduleItemRepository
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.lists.ListRepository
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

internal class MyScheduleManager(
    scheduleItemRepository: ScheduleItemRepository,
    private val remoteStateDispatcher: RemoteStateDispatcher,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val reminderUIManager: ReminderUIManager,
) : FavoritesManager<Long> {

    private val localStorage: LocalStorage
        get() = lazyLocalStorage.resolve()

    override val favoriteIds: Set<Long>
        get() = localStorage.project.event.myScheduleItemIds.value

    override val favoriteIdsFlow: Flow<Set<Long>>
        get() = localStorage.project.event.myScheduleItemIds.state

    override val repository: ListRepository<*> = scheduleItemRepository

    override fun addToFavorites(item: Favoriteable<Long>) {
        if (isInFavorites(item)) {
            return
        }
        localStorage.project.event.myScheduleItemIds.value += item.itemId
        reminderUIManager.onAddToMySchedule()
        remoteStateDispatcher.dispatch(MyScheduleRemoteStateEntry(favoriteIds))
    }

    override fun removeFromFavorites(item: Favoriteable<Long>) {
        if (!isInFavorites(item)) {
            return
        }
        localStorage.project.event.myScheduleItemIds.value -= item.itemId
        remoteStateDispatcher.dispatch(MyScheduleRemoteStateEntry(favoriteIds))
    }

    override fun isInFavorites(item: Favoriteable<Long>): Boolean =
        isInFavorites(item.itemId)

    override fun isInFavorites(itemId: Long): Boolean =
        localStorage.project.event.myScheduleItemIds.value.contains(itemId)

    companion object {
        const val diKey: String = "MySchedule"
    }
}

private class MyScheduleRemoteStateEntry(ids: Set<Long>) : RemoteStateEntry(
    key = "my_schedule_items",
    value = JsonArray(ids.map { JsonPrimitive(it) }),
    domain = Domain.PROJECT,
    isUrgent = false
)
