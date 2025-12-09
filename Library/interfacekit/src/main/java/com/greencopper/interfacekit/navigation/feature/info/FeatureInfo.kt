package com.greencopper.interfacekit.navigation.feature.info

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

public typealias FeatureParams = JsonElement

@Serializable
public data class FeatureInfo(
    val key: FeatureKey,
    val params: FeatureParams? = null,
    val onboarding: FeatureParams? = null
) {
    public fun withParams(newParams: JsonElement): FeatureInfo =
        FeatureInfo(key, newParams, onboarding)
}
