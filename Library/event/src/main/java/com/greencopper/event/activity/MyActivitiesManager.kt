package com.greencopper.event.activity

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.event.activity.data.repository.ActivityRepository
import com.greencopper.event.common.event
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.lists.ListRepository
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

internal class MyActivitiesManager(
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val remoteStateDispatcher: RemoteStateDispatcher,
    activitiesRepository: ActivityRepository,
) : FavoritesManager<Long> {

    private val localStorage: LocalStorage
        get() = lazyLocalStorage.resolve()

    override val favoriteIds: Set<Long>
        get() = localStorage.project.event.myActivities.value

    override val favoriteIdsFlow: Flow<Set<Long>>
        get() = localStorage.project.event.myActivities.state

    override val repository: ListRepository<ContentActivity> = activitiesRepository

    override fun addToFavorites(item: Favoriteable<Long>) {
        if (isInFavorites(item)) {
            return
        }
        localStorage.project.event.myActivities.value += item.itemId
        remoteStateDispatcher.dispatch(MyActivitiesIdsRemoteStateEntry(favoriteIds))
    }

    override fun removeFromFavorites(item: Favoriteable<Long>) {
        if (!isInFavorites(item)) {
            return
        }
        localStorage.project.event.myActivities.value -= item.itemId
        remoteStateDispatcher.dispatch(MyActivitiesIdsRemoteStateEntry(favoriteIds))
    }

    override fun isInFavorites(item: Favoriteable<Long>) =
        isInFavorites(item.itemId)

    override fun isInFavorites(itemId: Long) =
        localStorage.project.event.myActivities.value.contains(itemId)

    companion object {
        const val diKey: String = "MyActivities"
    }
}

private class MyActivitiesIdsRemoteStateEntry(ids: Set<Long>) : RemoteStateEntry(
    key = "my_activities",
    value = JsonArray(ids.map { JsonPrimitive(it) }),
    domain = Domain.PROJECT,
    isUrgent = false,
)
