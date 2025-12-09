package com.greencopper.interfacekit.navigation.feature

import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e

public interface FeatureInitializer {

    public val featureKey: FeatureKey

    /** Get [Layout] associated with this [FeatureInitializer] using [params]*/
    public fun getLayout(params: FeatureParams?): Layout

    /** Get [RedirectionHash] associated with those [params] or null if they don't apply.*/
    public fun redirectionHashFor(params: FeatureParams?): RedirectionHash
}

public abstract class ParameterizedFeatureInitializer<T> : FeatureInitializer {

    override fun getLayout(params: FeatureParams?): Layout {
        params ?: throw FeatureInitializerException.NoParametersProvidedException()
        val decodedParams = try {
            decodeParams(params)
        } catch (t: Throwable) {
            throw FeatureInitializerException.ParametersDecodeFailed(params)
        }

        return layoutForParams(decodedParams)
    }

    final override fun redirectionHashFor(params: FeatureParams?): RedirectionHash {
        params ?: return RedirectionHash(featureKey)

        return try {
            redirectionHashForParams(decodeParams(params))
        } catch (t: Throwable) {
            App.log.e(message = "Error parsing FeatureParams $params", throwable = t)
            RedirectionHash(featureKey)
        }
    }

    protected abstract fun decodeParams(params: FeatureParams): T
    protected abstract fun layoutForParams(params: T): Layout
    protected abstract fun redirectionHashForParams(params: T): RedirectionHash
}

public abstract class UnparameterizedFeatureInitializer : FeatureInitializer {

    override fun getLayout(params: FeatureParams?): Layout = getLayout()

    final override fun redirectionHashFor(params: FeatureParams?): RedirectionHash = RedirectionHash(featureKey)

    protected abstract fun getLayout(): Layout
}