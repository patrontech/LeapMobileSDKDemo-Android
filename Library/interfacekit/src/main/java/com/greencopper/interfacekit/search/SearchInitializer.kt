package com.greencopper.interfacekit.search

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.search.logic.SearchProviderInfo
import com.greencopper.interfacekit.search.ui.SearchFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class SearchInitializer : ParameterizedFeatureInitializer<SearchData>() {

    companion object {
        val key: FeatureKey = FeatureKey("InterfaceKit.Search", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): SearchData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: SearchData): Layout {
        return SearchFragment(
            SearchLayoutData(
                entries = params.providers,
                displayImages = params.displayImages,
                analytics = params.analytics,
                redirectionHash = redirectionHashForParams(params),
                emptySearchImage = params.emptySearchImage,
            )
        )
    }

    override fun redirectionHashForParams(params: SearchData): RedirectionHash =
        RedirectionHash(key, params.analytics.screenName)
}

@Serializable
internal data class SearchData(
    val providers: List<SearchProviderInfo>,
    val displayImages: Boolean = true,
    val analytics: ScreenNameAnalytics,
    val emptySearchImage: String,
) : KiboSerializable<SearchData> {

    override fun getSerializer(): KSerializer<SearchData> = serializer()
}

@Serializable
internal data class SearchLayoutData(
    val entries: List<SearchProviderInfo>,
    val displayImages: Boolean,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
    val emptySearchImage: String,
) : KiboSerializable<SearchLayoutData> {

    override fun getSerializer(): KSerializer<SearchLayoutData> = serializer()
}
