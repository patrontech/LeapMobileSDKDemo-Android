package com.greencopper.core.services.iplocation

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
internal data class IPLocationConfiguration(
   val endpoint: String
): KiboSerializable<IPLocationConfiguration> {
    override fun getSerializer(): KSerializer<IPLocationConfiguration> = serializer()
}
