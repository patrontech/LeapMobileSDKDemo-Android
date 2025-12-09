package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.search.logic.SearchEntry
import com.greencopper.interfacekit.search.logic.SearchProvider
import kotlinx.serialization.json.JsonElement

public class MockSearchProvider(
    public var mockedItems: List<SearchEntry> = emptyList()
): SearchProvider {
    override fun entries(encodedParams: JsonElement?): List<SearchEntry> = mockedItems
}
