package com.greencopper.interfacekit.search.logic

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class SearchEntry(
    val matches: List<String>,
    val viewData: ViewData
): KiboSerializable<SearchEntry> {

    override fun getSerializer(): KSerializer<SearchEntry> = serializer()

    @Serializable
    public sealed class ViewData {

        public abstract val sortingValue: String
        public abstract val routeLink: String

        @Serializable
        public data class TitleSubtitle(
            val title: String,
            val subtitle: String?,
            val image: String?,
            override val routeLink: String
        ): ViewData() {
            override val sortingValue: String
                get() = title.lowercase()
        }
    }
}
