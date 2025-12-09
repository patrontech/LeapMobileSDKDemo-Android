package com.greencopper.interfacekit.favorites

import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.lists.ListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

public interface FavoritesManager<T : Any> {

    public val favoriteIds: Set<T>
    public val favoriteIdsFlow: Flow<Set<T>>
    public val repository: ListRepository<*>

    public fun addToFavorites(item: Favoriteable<T>)
    public fun removeFromFavorites(item: Favoriteable<T>)
    public fun isInFavorites(item: Favoriteable<T>): Boolean
    public fun isInFavorites(itemId: T): Boolean
    public suspend fun getFavoritesWithPredicate(predicate: FilteringPredicate? = null): Set<T> {
        val items = repository.getListData(predicate)
            .first()
            .map { it.itemId } as List<T>
        return favoriteIds.intersect(items.toSet())
    }
}
