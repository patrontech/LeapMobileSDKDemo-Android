package com.greencopper.interfacekit.navigation.layout

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public interface RedirectingLayout {
    public val availableRedirections: List<RedirectionHash>
    public fun redirectTo(hash: RedirectionHash)
}

public interface RedirectableLayout {
    public val redirectionHash: RedirectionHash
}

@Serializable
public data class RedirectionHash(val featureKey: FeatureKey, val identifier: String? = null) :
    KiboSerializable<RedirectionHash> {
    override fun getSerializer(): KSerializer<RedirectionHash> = serializer()
}