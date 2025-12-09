package com.greencopper.ticketing.providers.showclix

import com.greencopper.ticketing.providers.showclix.data.ShowclixFetchedTickets
import com.greencopper.ticketing.providers.showclix.data.VerifyTokenData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

internal interface ShowclixAPI {

    @POST
    suspend fun getMagicLinkUrl(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body body: ShowclixRequest.MagicLinkBody,
    )

    @POST
    suspend fun verifyToken(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body body: ShowclixRequest.TokenBody,
    ) : VerifyTokenData

    @GET
    suspend fun getTickets(
        @Url url: String,
        @Header("Authorization") authorization: String,
    ) : ShowclixFetchedTickets
}

internal class ShowclixRequest {

    @Serializable
    data class MagicLinkBody(
        @SerialName("user_email") val email: String,
        val link: String,
    )

    @Serializable
    data class TokenBody(val token: String)
}
