package com.greencopper.interfacekit.navigation.feature

import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.layout.Layout

public interface FeatureResolver {
    public fun resolve(info: FeatureInfo): Layout
    public fun resolveInitializer(info: FeatureInfo): FeatureInitializer
}