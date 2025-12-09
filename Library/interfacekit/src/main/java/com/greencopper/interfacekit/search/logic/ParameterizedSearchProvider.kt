package com.greencopper.interfacekit.search.logic

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement

public interface ParameterizedSearchProvider<T: KiboSerializable<T>>: SearchProvider {

    public fun entries(params: T): List<SearchEntry>

    override fun entries(encodedParams: JsonElement?): List<SearchEntry> {
        encodedParams ?: throw ParameterizedSearchProviderException.NoParametersProvidedException()
        val decodedParams = try {
            deserialize(encodedParams)
        } catch (t: Throwable) {
            throw ParameterizedSearchProviderException.ParametersDecodeFailed(encodedParams)
        }
        return entries(decodedParams)
    }

    @Throws(SerializationException::class, ClassCastException::class)
    public fun deserialize(encodedParams: JsonElement): T
}

internal sealed class ParameterizedSearchProviderException : Throwable() {
    class NoParametersProvidedException : ParameterizedSearchProviderException()

    internal class ParametersDecodeFailed(private val params: JsonElement? = null) : ParameterizedSearchProviderException() {
        override val message: String
            get() = "[ParameterizedSearchProviderException] Couldn't decode parameters $params"
    }
}
