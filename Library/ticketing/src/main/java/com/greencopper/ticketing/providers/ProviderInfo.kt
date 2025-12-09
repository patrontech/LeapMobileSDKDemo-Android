package com.greencopper.ticketing.providers

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class ProviderInfo(
    val key: Key,
    val params: ProviderParams?
): KiboSerializable<ProviderInfo> {
    override fun getSerializer(): KSerializer<ProviderInfo> = serializer()

    @Serializable
    public data class Key(val name: String, val version: Int)
}

