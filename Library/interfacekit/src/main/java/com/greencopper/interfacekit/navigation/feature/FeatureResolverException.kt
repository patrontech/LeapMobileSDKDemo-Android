package com.greencopper.interfacekit.navigation.feature

import com.greencopper.interfacekit.navigation.feature.info.FeatureKey

public sealed class FeatureResolverException : Throwable() {
    internal class FeatureNotRegisteredException(val key: FeatureKey) : Throwable() {
        override val message: String
            get() = "[FeatureResolverException] Couldn't find Initializer associated with key $key."
    }
}
