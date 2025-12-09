package com.greencopper.interfacekit.navigation.feature.info

import com.greencopper.core.content.Key
import kotlinx.serialization.Serializable

@Serializable
public data class FeatureKey(override val name: String, override val version: Int) : Key()