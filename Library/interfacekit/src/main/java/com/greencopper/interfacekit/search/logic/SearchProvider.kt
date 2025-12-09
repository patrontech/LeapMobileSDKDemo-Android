package com.greencopper.interfacekit.search.logic

import kotlinx.serialization.json.JsonElement

public interface SearchProvider {

    public fun entries(encodedParams: JsonElement?): List<SearchEntry>

}
