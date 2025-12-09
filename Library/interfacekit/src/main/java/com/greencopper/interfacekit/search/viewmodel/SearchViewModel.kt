package com.greencopper.interfacekit.search.viewmodel

import androidx.lifecycle.ViewModel
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.interfacekit.search.logic.SearchEntry
import com.greencopper.interfacekit.search.logic.SearchProvider
import com.greencopper.interfacekit.search.logic.SearchProviderInfo
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.d
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import me.xdrop.fuzzywuzzy.FuzzySearch

internal class SearchViewModel(
    providers: List<SearchProviderInfo>,
    val conditionChecker: ConditionChecker,
): ViewModel() {

    private val entries: List<SearchEntry> = providers.flatMap {
        try {
            val provider = App.resolve<SearchProvider>(tag = it.key)
            provider.entries(it.encodedParams)
        } catch (throwable: Throwable) {
            App.log.d("Error while resolving provider and entries", throwable = throwable)
            emptyList()
        }
    }

    val queryPattern: MutableStateFlow<String> = MutableStateFlow("")

    fun getSearchedEntries(): Flow<List<SearchEntry.ViewData>> {
        return queryPattern.map { query ->
            val formattedQuery = query.trim()
            if (formattedQuery.isBlank()) {
                entries.map { it.viewData }.sortedBy { it.sortingValue }
            } else {
                entries.mapNotNull { entry ->
                    FuzzySearch.extractSorted(formattedQuery, entry.matches, SEARCH_SCORE_THRESHOLD).firstOrNull()?.let {
                        Pair(it.score, entry)
                    }
                }.sortedWith(
                    compareByDescending<Pair<Int, SearchEntry>> { it.first }
                        .thenBy { it.second.viewData.sortingValue }
                ).map {
                    it.second.viewData
                }
            }
        }

    }

    private companion object {
        const val SEARCH_SCORE_THRESHOLD = 60
    }
}
