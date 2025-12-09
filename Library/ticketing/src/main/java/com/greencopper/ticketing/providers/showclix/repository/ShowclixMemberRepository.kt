package com.greencopper.ticketing.providers.showclix.repository

import com.greencopper.ticketing.models.Ticket
import com.greencopper.ticketing.providers.showclix.data.VerifyTokenData

public interface ShowclixMemberRepository {

    public suspend fun sendMagicLink(url: String, email: String, magicLink: String): Boolean
    public suspend fun verifyToken(url: String, token: String): VerifyTokenData?
    public suspend fun fetchTickets(url: String): List<Ticket>
}