package com.greencopper.thuzi.badges.initializer

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.thuzi.badges.ui.BadgesFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class BadgesInitializer : ParameterizedFeatureInitializer<BadgesData>() {

    companion object {
        val key = FeatureKey("Thuzi.Badges", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): BadgesData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: BadgesData): Layout = BadgesFragment(
        BadgesLayoutData(
            analytics = params.analytics,
            url = params.url,
            redirectionHash = redirectionHashForParams(params)
        )
    )

    override fun redirectionHashForParams(params: BadgesData): RedirectionHash =
        RedirectionHash(key, params.url)
}

@Serializable
internal data class BadgesData(
    val analytics: ScreenNameAnalytics,
    val url: String,
) : KiboSerializable<BadgesData> {

    override fun getSerializer(): KSerializer<BadgesData> = serializer()
}

@Serializable
internal data class BadgesLayoutData(
    val analytics: ScreenNameAnalytics,
    val url: String,
    val redirectionHash: RedirectionHash
) : KiboSerializable<BadgesLayoutData> {
    override fun getSerializer(): KSerializer<BadgesLayoutData> = serializer()
}
