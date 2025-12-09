package com.greencopper.interfacekit.search.viewmodel

import com.greencopper.interfacekit.search.logic.SearchEntry
import com.greencopper.interfacekit.search.logic.SearchProvider
import com.greencopper.interfacekit.search.logic.SearchProviderInfo
import com.greencopper.interfacekit.search.logic.SearchProviderKey
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.core.MockConditionChecker
import com.greencopper.testmocks.interfacekit.MockSearchProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SearchViewModelTest {
    private val conditionChecker = MockConditionChecker()

    private val allSearchProvidersInfo = listOf(
        SearchProviderInfo(
            SearchProviderKey("key1",1),
            JsonNull
        ),
        SearchProviderInfo(
            SearchProviderKey("key2",1),
            JsonNull
        ),
        SearchProviderInfo(
            SearchProviderKey("key3",1),
            JsonNull
        ),
    )

    private val searchEntry1ViewData = SearchEntry.ViewData.TitleSubtitle(
        title = "searchEntry1",
        subtitle = null,
        image = null,
        routeLink = "searchEntry1routeLink",
    )

    private val searchEntry2ViewData = SearchEntry.ViewData.TitleSubtitle(
        title = "searchEntry2",
        subtitle = null,
        image = null,
        routeLink = "searchEntry2routeLink",
    )

    private val searchProvider1: SearchProvider = MockSearchProvider(
        listOf(
            SearchEntry(
                matches = listOf("searchEntry1"),
                viewData = searchEntry1ViewData
            )
        )
    )

    private val searchProvider2: SearchProvider = MockSearchProvider(
        listOf(
            SearchEntry(
                matches = listOf("searchEntry2"),
                viewData = searchEntry2ViewData
            )
        )
    )

    init {
        Toolkit.setupTest()
        bindProvider(searchProvider1, tag = SearchProviderKey("key1",1))
        bindProvider(searchProvider2, tag = SearchProviderKey("key2",1))
    }

    @Test
    fun getSearchedEntries_whenNoProviders_shouldReturnEmpty() {
        runTest {
            val classUnderTest = SearchViewModel(emptyList(), conditionChecker)
            val result = classUnderTest.getSearchedEntries().first()
            assertThat(result.isEmpty()).isTrue
        }
    }

    @Test
    fun getSearchedEntries_whenQueryPatternIsBlank_shouldReturnAll() {
        runTest {
            val classUnderTest = SearchViewModel(allSearchProvidersInfo, conditionChecker)
            val result = classUnderTest.getSearchedEntries().first()
            assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(listOf(searchEntry1ViewData, searchEntry2ViewData))
        }
    }

    @Test
    fun getSearchedEntries_whenUsingFuzzySearch_shouldReturnEmpty() {
        runTest {
            val classUnderTest = SearchViewModel(allSearchProvidersInfo, conditionChecker)
            classUnderTest.queryPattern.value = "test"
            val result = classUnderTest.getSearchedEntries().first()
            assertThat(result.isEmpty()).isTrue
        }
    }

    @Test
    fun getSearchedEntries_whenUsingFuzzySearch_shouldReturnAll_StartingFromSearchEntry2() {
        runTest {
            val classUnderTest = SearchViewModel(allSearchProvidersInfo, conditionChecker)
            classUnderTest.queryPattern.value = "searchEntry2"
            val result = classUnderTest.getSearchedEntries().first()
            assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(listOf(searchEntry2ViewData, searchEntry1ViewData))
        }
    }
}
