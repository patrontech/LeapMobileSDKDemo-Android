package com.greencopper.thuzi.eventpass.initializer

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.thuzi.eventpass.ui.EventPassFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class EventPassInitializer : ParameterizedFeatureInitializer<EventPassData>() {

    internal companion object {
        internal val key: FeatureKey = FeatureKey("Thuzi.EventPass", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): EventPassData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: EventPassData): Layout = EventPassFragment(
        EventPassLayoutData(
            params.analytics,
            redirectionHash = redirectionHashForParams(params)
        )
    )

    override fun redirectionHashForParams(params: EventPassData): RedirectionHash {
        return RedirectionHash(key)
    }
}

@Serializable
internal data class EventPassData(val analytics: ScreenNameAnalytics) : KiboSerializable<EventPassData> {

    override fun getSerializer(): KSerializer<EventPassData> = serializer()
}

@Serializable
internal data class EventPassLayoutData(
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash
) : KiboSerializable<EventPassLayoutData> {
    override fun getSerializer(): KSerializer<EventPassLayoutData> = serializer()
}
