package com.greencopper.ticketingmocks.providers

import com.greencopper.core.data.KiboSerializable
import com.greencopper.ticketing.models.Ticket
import com.greencopper.ticketing.providers.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public class MockProvider : ParameterizedProvider<MockProvider.Data>() {

    @Serializable
    public data class Data(val apiUrl: String) : KiboSerializable<Data> {
        override fun getSerializer(): KSerializer<Data> = serializer()
    }

    public var cachedTicketsMock: List<Ticket> = emptyList()
    override val cachedTickets: List<Ticket>
        get() = cachedTicketsMock

    public var setupFun: (params: ProviderParams?) -> Unit = { _: ProviderParams? ->
    }

    override fun setup(params: ProviderParams?) {
        setupFun(params)
    }

    public var logoutFun: () -> Unit = {}
    override suspend fun logout() {
        logoutFun()
    }

    public var fetchTicketsFun: () -> List<Ticket> = {
        emptyList()
    }
    override suspend fun fetchTickets(): List<Ticket> {
        return fetchTicketsFun()
    }

    public companion object {
        public val key: ProviderInfo.Key = ProviderInfo.Key("Ticketing.Provider.MockProvider", 1)
    }

    public lateinit var data: Data
    override fun setup(data: Data) {
        this.data = data
    }

    override fun deserialize(providerParameters: ProviderParams): Data = KiboSerializable.decodeFromJsonElement(providerParameters)
}
