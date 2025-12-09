package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.navigation.feature.FeatureInitializer
import com.greencopper.interfacekit.navigation.feature.FeatureResolver
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.layout.Layout

public class MockFeatureResolver(
    public var layout: Layout? = null,
    public var featureInitializer: FeatureInitializer? = null,
): FeatureResolver {
    override fun resolve(info: FeatureInfo): Layout =
        layout ?: throw NotImplementedError()

    override fun resolveInitializer(info: FeatureInfo): FeatureInitializer =
        featureInitializer ?: throw NotImplementedError()
}
