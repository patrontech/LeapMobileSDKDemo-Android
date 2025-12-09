package com.greencopper.interfacekit.search.logic

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class SearchProviderInfo(
    val key: SearchProviderKey,
    @SerialName("params") val encodedParams: JsonElement? = null
): KiboSerializable<SearchProviderInfo> {

    constructor(key: SearchProviderKey, params: KiboSerializable<*>? = null) : this(
        key,
        params?.encodeToJsonElement()
    )

    override fun getSerializer(): KSerializer<SearchProviderInfo> = serializer()

}
