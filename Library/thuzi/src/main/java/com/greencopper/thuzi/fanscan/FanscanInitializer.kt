package com.greencopper.thuzi.fanscan

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.thuzi.fanscan.ui.fragment.FanscanFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class FanscanInitializer : ParameterizedFeatureInitializer<FanscanData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Thuzi.Fanscan", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): FanscanData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: FanscanData): Layout = FanscanFragment(
        FanscanLayoutData(
            checkinUrl = params.checkinUrl,
            analytics = params.analytics,
            successPage = params.successPage,
            redirectionHash = redirectionHashForParams(params)
        )
    )

    override fun redirectionHashForParams(params: FanscanData): RedirectionHash =
        RedirectionHash(key, params.checkinUrl)
}

@Serializable
internal data class SuccessPage(
    val redirectionTitle: String,
    val redirectionUrl: String
)

@Serializable
internal data class FanscanData(
    val checkinUrl: String,
    val analytics: ScreenNameAnalytics,
    val successPage: SuccessPage? = null
) : KiboSerializable<FanscanData> {

    override fun getSerializer(): KSerializer<FanscanData> = serializer()
}

@Serializable
internal data class FanscanLayoutData(
    val checkinUrl: String,
    val analytics: ScreenNameAnalytics,
    val successPage: SuccessPage?,
    val redirectionHash: RedirectionHash
) : KiboSerializable<FanscanLayoutData> {
    override fun getSerializer(): KSerializer<FanscanLayoutData> = serializer()
}
