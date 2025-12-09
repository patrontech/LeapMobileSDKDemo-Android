package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.lists.ListRepository
import kotlinx.coroutines.flow.*

public class MockFavoritesManager<T : Any>(
    override var favoriteIds: MutableSet<T> = mutableSetOf(),
    override var repository: ListRepository<*> = MockListRepository(),
) : FavoritesManager<T> {
    private val _favoriteIdsFlow = MutableStateFlow(favoriteIds.toMutableSet())
    override val favoriteIdsFlow: Flow<Set<T>> = _favoriteIdsFlow.asStateFlow()

    public var addToFavoritesCallCount: Int = 0
    public var removeFromFavoritesCallCount: Int = 0

    override fun addToFavorites(item: Favoriteable<T>) {
        addToFavoritesCallCount++
        favoriteIds.add(item.itemId)
        _favoriteIdsFlow.value = favoriteIds.plus(item.itemId).toMutableSet()
    }

    override fun removeFromFavorites(item: Favoriteable<T>) {
        removeFromFavoritesCallCount++
        favoriteIds.remove(item.itemId)
        _favoriteIdsFlow.value = favoriteIds.minus(item.itemId).toMutableSet()
    }

    override fun isInFavorites(item: Favoriteable<T>): Boolean =
        favoriteIds.contains(item.itemId)

    override fun isInFavorites(itemId: T): Boolean = favoriteIds.contains(itemId)
}

public data class MockFavoriteable<T>(override val itemId: T) : Favoriteable<T>
