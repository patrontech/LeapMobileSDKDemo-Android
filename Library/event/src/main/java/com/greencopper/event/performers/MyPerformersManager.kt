package com.greencopper.event.performers

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.event.common.event
import com.greencopper.event.performers.data.repository.PerformerRepository
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.lists.ListRepository
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

internal class MyPerformersManager(
    private val remoteStateDispatcher: RemoteStateDispatcher,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    performersRepository: PerformerRepository,
) : FavoritesManager<String> {

    private val localStorage: LocalStorage
        get() = lazyLocalStorage.resolve()

    override val favoriteIds: Set<String>
        get() = localStorage.project.event.myPerformers.value

    override val favoriteIdsFlow: Flow<Set<String>>
        get() = localStorage.project.event.myPerformers.state

    override val repository: ListRepository<*> = performersRepository

    override fun addToFavorites(item: Favoriteable<String>) {
        if (isInFavorites(item)) {
            return
        }
        localStorage.project.event.myPerformers.value += item.itemId
        remoteStateDispatcher.dispatch(MyPerformersIdsRemoteStateEntry(favoriteIds))
    }

    override fun removeFromFavorites(item: Favoriteable<String>) {
        if (!isInFavorites(item)) {
            return
        }
        localStorage.project.event.myPerformers.value -= item.itemId
        remoteStateDispatcher.dispatch(MyPerformersIdsRemoteStateEntry(favoriteIds))
    }

    override fun isInFavorites(item: Favoriteable<String>): Boolean =
        isInFavorites(item.itemId)

    override fun isInFavorites(itemId: String): Boolean =
        localStorage.project.event.myPerformers.value.contains(itemId)

    companion object {
        const val diKey: String = "MyPerformers"
    }
}


private class MyPerformersIdsRemoteStateEntry(ids: Set<String>) : RemoteStateEntry(
    key = "my_performers",
    value = JsonArray(ids.map { JsonPrimitive(it) }),
    domain = Domain.PROJECT,
    isUrgent = false,
)
