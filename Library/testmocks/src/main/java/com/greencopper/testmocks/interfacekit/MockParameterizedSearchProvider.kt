package com.greencopper.testmocks.interfacekit

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.search.logic.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

public class MockParameterizedSearchProvider : ParameterizedSearchProvider<MockParameterizedSearchProvider.Params> {

        @Serializable
        public data class Params(
            val value: List<String>
        ): KiboSerializable<Params> {
            override fun getSerializer(): KSerializer<Params> = serializer()
        }

        override fun entries(params: Params): List<SearchEntry> = params.value.map {
            SearchEntry(
                listOf(it),
                SearchEntry.ViewData.TitleSubtitle(
                    title = it,
                    subtitle = null,
                    image = null,
                    routeLink = it
                )
            )
        }

        override fun deserialize(encodedParams: JsonElement): Params =
            KiboSerializable.decodeFromJsonElement(encodedParams)

    public companion object {
        public val key: SearchProviderKey = SearchProviderKey("Mock.SearchProvider", 1)
        }
    }
