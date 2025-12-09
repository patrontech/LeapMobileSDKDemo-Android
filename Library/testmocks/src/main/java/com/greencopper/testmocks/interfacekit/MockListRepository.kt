package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.lists.ListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockListRepository(public val dataList: List<MockListItem> = emptyList()) : ListRepository<MockListItem> {
    public var getListDataCallCount: Int = 0

    override suspend fun getListData(predicate: FilteringPredicate?): Flow<List<MockListItem>> = flowOf(dataList).also {
        getListDataCallCount++
    }

}

public data class MockListItem(override val itemId: String) : ListRepository.Item<String>
