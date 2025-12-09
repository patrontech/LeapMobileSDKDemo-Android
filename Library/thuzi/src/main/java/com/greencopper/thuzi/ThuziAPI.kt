package com.greencopper.thuzi

import com.greencopper.thuzi.services.attendee.VirtualAccessCard
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

internal interface ThuziAPI {

    @GET
    suspend fun getAttendee(
        @Url url: String,
        @Header("Authorization") authorizationHeader: String,
    ) : ThuziResponse.Attendee

    @GET
    suspend fun getProfile(
        @Url url: String,
        @Header("Authorization") authorizationHeader: String,
    ) : Map<String, String>

    @POST
    suspend fun deleteAccount(
        @Url url: String,
        @Header("Authorization") authorizationHeader: String,
        @Body body: ThuziRequest.AccountDeletion,
    )

    @GET
    suspend fun getBadges(@Url url: String): ThuziResponse.Badges

    @Streaming
    @GET
    suspend fun getBadgeImage(@Url url: String): Response<ResponseBody>

    @POST
    suspend fun checkIn(
        @Url url: String,
        @Header("Authorization") authorizationHeader: String,
    )

    @POST
    suspend fun copyRoutine(
        @Url url: String,
        @Header("Authorization") authorizationHeader: String,
    ) : ThuziResponse.CopyResponse
}

internal sealed class ThuziRequest {

    @Serializable
    class AccountDeletion(
        val locale: String,
        @SerialName("installation_id")
        val installationId: String,
        val email: String,
        @SerialName("attendee_id")
        val attendeeId: String
    )
}

internal sealed class ThuziResponse {

    @Serializable
    data class Attendee(
        val email: String,
        val postalCode: String? = null,
        val customAnswers: List<String?> = emptyList(),
        val virtualAccessCards: List<VirtualAccessCard> = emptyList(),
        val firstName: String? = null,
        val lastName: String? = null,
    )

    @Serializable
    data class Badges(
        val id: String,
        val name: String,
        val badges: List<Badge>
    )

    @Serializable
    data class Badge(
        val badgeId: String,
        val name: String? = null,
        val earnedDescription: String? = null,
        val unearnedDescription: String? = null,
        val isEarned: Boolean,
        val earnedOn: String? = null,
        val earnedImageUrl: String? = null,
        val unearnedImageUrl: String? = null
    )

    @Serializable
    data class CopyResponse(val authToken: String)
}

