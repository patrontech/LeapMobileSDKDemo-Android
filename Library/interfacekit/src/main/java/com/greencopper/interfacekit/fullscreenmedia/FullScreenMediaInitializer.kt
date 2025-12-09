package com.greencopper.interfacekit.fullscreenmedia

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.fullscreenmedia.ui.FullScreenMediaFragment
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class FullScreenMediaInitializer : ParameterizedFeatureInitializer<FullScreenMediaData>() {

    companion object {
        val key: FeatureKey = FeatureKey("InterfaceKit.FullScreenMedia", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): FullScreenMediaData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: FullScreenMediaData): Layout {
        val layoutData = FullScreenMediaLayoutData(
            mediaName = params.name,
            analytics = params.analytics,
            redirectionHash = redirectionHashForParams(params)
        )

        return FullScreenMediaFragment(layoutData)
    }

    override fun redirectionHashForParams(params: FullScreenMediaData): RedirectionHash =
        RedirectionHash(key, params.name)


}

@Serializable
internal data class FullScreenMediaData(
    val name: String,
    val analytics: ScreenNameAnalytics
) : KiboSerializable<FullScreenMediaData> {

    override fun getSerializer(): KSerializer<FullScreenMediaData> = serializer()
}

@Serializable
internal data class FullScreenMediaLayoutData(
    val mediaName: String,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash
) :
    KiboSerializable<FullScreenMediaLayoutData> {
    override fun getSerializer(): KSerializer<FullScreenMediaLayoutData> = serializer()
}
