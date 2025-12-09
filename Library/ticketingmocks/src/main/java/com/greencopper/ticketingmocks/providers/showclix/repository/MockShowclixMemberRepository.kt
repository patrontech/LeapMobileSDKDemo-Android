package com.greencopper.ticketingmocks.providers.showclix.repository

import com.greencopper.ticketing.models.Ticket
import com.greencopper.ticketing.providers.showclix.data.VerifyTokenData
import com.greencopper.ticketing.providers.showclix.repository.ShowclixMemberRepository

public class MockShowclixMemberRepository : ShowclixMemberRepository {

    public var sendMagicLinkFun: (url: String, email: String, magicLink: String) -> Boolean =
        { _: String, _: String, _: String ->
            false
        }

    override suspend fun sendMagicLink(url: String, email: String, magicLink: String): Boolean {
        return sendMagicLinkFun(url, email, magicLink)
    }

    public var verifyTokenFun: (url: String, token: String) -> VerifyTokenData? =
        { _: String, _: String ->
            null
        }

    override suspend fun verifyToken(url: String, token: String): VerifyTokenData? {
        return verifyTokenFun(url, token)
    }

    public var fetchTicketsFun: (url: String) -> List<Ticket> =
        { _: String ->
            emptyList()
        }

    override suspend fun fetchTickets(url: String): List<Ticket> {
        return fetchTicketsFun(url)
    }

}
