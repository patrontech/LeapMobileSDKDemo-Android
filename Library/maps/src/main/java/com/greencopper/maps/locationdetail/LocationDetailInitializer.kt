package com.greencopper.maps.locationdetail

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.tags.DisplayableTag
import com.greencopper.maps.locationdetail.ui.LocationDetailFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class LocationDetailInitializer : ParameterizedFeatureInitializer<LocationDetailData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Maps.LocationDetail", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): LocationDetailData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: LocationDetailData): Layout = LocationDetailFragment(
        LocationDetailLayoutData(
            locationId = params.locationId,
            analytics = params.analytics,
            redirectionHash = redirectionHashForParams(params),
            favoritesEditing = params.favoritesEditing,
            displayableTags = params.displayableTags,
        )
    )

    override fun redirectionHashForParams(params: LocationDetailData): RedirectionHash =
        RedirectionHash(key, params.locationId)
}

@Serializable
internal data class LocationDetailData(
    val locationId: String,
    val analytics: ScreenNameAnalytics,
    val favoritesEditing: FavoritesEditing? = null,
    val displayableTags: List<DisplayableTag> = emptyList(),
) : KiboSerializable<LocationDetailData> {

    override fun getSerializer(): KSerializer<LocationDetailData> = serializer()
}

@Serializable
internal data class LocationDetailLayoutData(
    val locationId: String,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
    val favoritesEditing: FavoritesEditing?,
    val displayableTags: List<DisplayableTag>,
) : KiboSerializable<LocationDetailLayoutData> {

    override fun getSerializer(): KSerializer<LocationDetailLayoutData> = serializer()
}
