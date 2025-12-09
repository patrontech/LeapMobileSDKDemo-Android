package com.greencopper.interfacekit.links

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.route.Route
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class LinksConfiguration(
    internal val routeLinks: Map<String, Route>,
    internal val featureLinks: Map<String, FeatureInfo>
) : KiboSerializable<LinksConfiguration> {

    override fun getSerializer(): KSerializer<LinksConfiguration> = serializer()
}
