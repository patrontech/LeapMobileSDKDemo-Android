package com.greencopper.thuzi.logout

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.thuzi.logout.ui.LogoutFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class LogoutInitializer : ParameterizedFeatureInitializer<LogoutLayoutData>() {
    companion object {
        val key: FeatureKey = FeatureKey("Thuzi.Logout", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): LogoutLayoutData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: LogoutLayoutData): Layout = LogoutFragment(params)

    override fun redirectionHashForParams(params: LogoutLayoutData): RedirectionHash =
        RedirectionHash(featureKey, params.analytics.screenName)
}

@Serializable
internal data class LogoutLayoutData(
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<LogoutLayoutData> {

    override fun getSerializer(): KSerializer<LogoutLayoutData> = serializer()
}
