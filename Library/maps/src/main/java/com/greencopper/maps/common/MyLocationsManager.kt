package com.greencopper.maps.common

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.lists.ListRepository
import com.greencopper.maps.recipe.MapsRepository
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

internal class MyLocationsManager(
    private val remoteStateDispatcher: RemoteStateDispatcher,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    mapsRepository: MapsRepository,
) : FavoritesManager<String> {
    private val localStorage: LocalStorage
        get() = lazyLocalStorage.resolve()

    override val favoriteIds: Set<String>
        get() = localStorage.project.maps.myLocations.value

    override val favoriteIdsFlow: Flow<Set<String>>
        get() = localStorage.project.maps.myLocations.state

    override val repository: ListRepository<*> = mapsRepository

    override fun isInFavorites(itemId: String): Boolean =
        favoriteIds.contains(itemId)

    override fun isInFavorites(item: Favoriteable<String>): Boolean =
        isInFavorites(item.itemId)

    override fun removeFromFavorites(item: Favoriteable<String>) {
        if (!isInFavorites(item)) {
            return
        }
        localStorage.project.maps.myLocations.value -= item.itemId
        remoteStateDispatcher.dispatch(MyLocationsIdsRemoteStateEntry(favoriteIds))
    }

    override fun addToFavorites(item: Favoriteable<String>) {
        if (isInFavorites(item)) {
            return
        }
        localStorage.project.maps.myLocations.value += item.itemId
        remoteStateDispatcher.dispatch(MyLocationsIdsRemoteStateEntry(favoriteIds))
    }

    companion object {
        const val diKey: String = "MyLocations"
    }
}

private class MyLocationsIdsRemoteStateEntry(ids: Set<String>) : RemoteStateEntry(
    key = "my_locations",
    value = JsonArray(ids.map { JsonPrimitive(it) }),
    domain = Domain.PROJECT,
    isUrgent = false,
)
