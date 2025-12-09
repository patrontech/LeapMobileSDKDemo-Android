package com.greencopper.core.networking

import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.services.iplocation.IPLocation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Streaming
import retrofit2.http.Url

public interface CoreAPI {

    @GET
    public suspend fun getOTAContent(@Url url: String): List<OTAContent>

    @GET
    public suspend fun getDraftOTAContent(
        @Url url: String,
        @Header("Authorization") authHeader: String,
    ): List<OTAContent>

    @Streaming
    @GET
    public suspend fun downloadFile(@Url url: String): Response<ResponseBody>

    @Streaming
    @GET
    public suspend fun downloadDraftFile(
        @Url url: String,
        @Header("Authorization") authHeader: String,
    ): Response<ResponseBody>

    @PATCH
    @JvmSuppressWildcards
    public suspend fun sendUserState(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body body: Map<String, JsonElement?>,
    )

    @POST
    public suspend fun getIPLocation(@Url url: String): IPLocation

    @PUT
    public suspend fun registerNotifications(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body request: CoreRequest.Registration,
    )

    @DELETE
    public suspend fun unregisterNotifications(
        @Url url: String,
        @Header("Authorization") authHeader: String,
    )
}

public sealed class CoreRequest {

    @Serializable
    public data class Registration(
        @SerialName("registration_token") val registrationToken: String? = null,
        val platform: String,
        val locale: String,
        @SerialName("is_optin") val isOptin: Boolean? = null,
    )
}
