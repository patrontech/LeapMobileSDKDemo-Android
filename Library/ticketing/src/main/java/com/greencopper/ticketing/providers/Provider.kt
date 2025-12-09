package com.greencopper.ticketing.providers

import com.greencopper.core.data.KiboSerializable
import com.greencopper.ticketing.models.Ticket
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement

public sealed class Provider {

    public abstract val cachedTickets: List<Ticket>

    public abstract fun setup(params: ProviderParams?)
    public abstract suspend fun logout()

    @Throws(ProviderException::class)
    public abstract suspend fun fetchTickets(): List<Ticket>
}

public abstract class ParameterizedProvider<T: KiboSerializable<T>>: Provider() {
    public abstract fun setup(data: T)

    override fun setup(params: ProviderParams?) {
        if(params == null) throw ProviderException.ParamsRequiredException()
        setup(deserialize(params))
    }

    @Throws(SerializationException::class, ClassCastException::class)
    public abstract fun deserialize(providerParameters: ProviderParams): T
}

public typealias ProviderParams = JsonElement

public open class ProviderException(throwable: Throwable? = null) : Exception(throwable) {
    public class TokenExpiredException(throwable: Throwable? = null): ProviderException(throwable)
    public class ParamsRequiredException(throwable: Throwable? = null): ProviderException(throwable)
}