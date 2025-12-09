package com.greencopper.ticketing.providers.showclix

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.Email
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.user
import com.greencopper.ticketing.models.Ticket
import com.greencopper.ticketing.providers.ParameterizedProvider
import com.greencopper.ticketing.providers.ProviderException
import com.greencopper.ticketing.providers.ProviderInfo
import com.greencopper.ticketing.providers.ProviderParams
import com.greencopper.ticketing.providers.showclix.repository.ShowclixMemberRepository
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import retrofit2.HttpException
import java.net.HttpURLConnection

internal class ShowclixProvider(
    private val showclixMemberRepository: ShowclixMemberRepository,
    private val localStorage: LocalStorage,
) : ParameterizedProvider<ShowclixProvider.Data>() {

    private lateinit var data: Data

    @Serializable
    data class Data(val apiUrl: String): KiboSerializable<Data> {
        override fun getSerializer(): KSerializer<Data> = serializer()
    }

    override suspend fun logout() {
        localStorage.project.showclix.validationToken.value = null
        localStorage.project.showclix.userId.value = null
        localStorage.project.user.putEmail(Email.SHOWCLIX, null)
        localStorage.project.showclix.tickets.value = emptyList()
    }

    override val cachedTickets: List<Ticket>
        get() = localStorage.project.showclix.tickets.value

    @Throws(ProviderException::class)
    override suspend fun fetchTickets(): List<Ticket> {
        try {
            val fetchTickets = showclixMemberRepository.fetchTickets(data.apiUrl)
            localStorage.project.showclix.tickets.value = fetchTickets
            return fetchTickets
        }
        catch (throwable: Throwable) {
            if((throwable as? HttpException)?.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw ProviderException.TokenExpiredException(throwable)
            } else {
                throw ProviderException(throwable)
            }
        }
    }

    override fun setup(data: Data) {
        this.data = data
    }

    override fun deserialize(providerParameters: ProviderParams): Data =
        KiboSerializable.decodeFromJsonElement(providerParameters)

    companion object {
        val key = ProviderInfo.Key("Ticketing.Provider.Showclix", 1)
    }

}
