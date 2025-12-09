package com.greencopper.ticketing.mocks

import com.greencopper.ticketing.providers.showclix.ShowclixAPI
import com.greencopper.ticketing.providers.showclix.ShowclixRequest
import com.greencopper.ticketing.providers.showclix.data.ShowclixFetchedTickets
import com.greencopper.ticketing.providers.showclix.data.VerifyTokenData

internal class MockShowclixAPI(
    var verifyTokenRequest: () -> VerifyTokenData = { throw NotImplementedError() },
    var getTicketsRequest: () -> ShowclixFetchedTickets = { throw NotImplementedError() },
    var getMagicLinkUrlRequest: () -> Unit = {}
) : ShowclixAPI {

    var verifyTokenCount = 0
    var getMagicLinkUrlCount = 0

    override suspend fun verifyToken(
        url: String,
        authorization: String,
        body: ShowclixRequest.TokenBody
    ): VerifyTokenData {
        verifyTokenCount += 1
        return verifyTokenRequest()
    }

    override suspend fun getTickets(url: String, authorization: String): ShowclixFetchedTickets =
        getTicketsRequest()

    override suspend fun getMagicLinkUrl(url: String, authorization: String, body: ShowclixRequest.MagicLinkBody) {
        getMagicLinkUrlCount += 1
        getMagicLinkUrlRequest()
    }
}
