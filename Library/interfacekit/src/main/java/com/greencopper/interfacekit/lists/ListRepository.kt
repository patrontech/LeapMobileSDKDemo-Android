package com.greencopper.interfacekit.lists

import com.greencopper.interfacekit.filtering.FilteringPredicate
import kotlinx.coroutines.flow.Flow

public interface ListRepository<T : ListRepository.Item<*>> {
    public suspend fun getListData(predicate: FilteringPredicate? = null): Flow<List<T>>

    public interface Item<V> {
        public val itemId: V
    }
}
