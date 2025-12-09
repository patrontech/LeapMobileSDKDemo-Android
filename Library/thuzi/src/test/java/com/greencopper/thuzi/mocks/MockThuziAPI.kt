package com.greencopper.thuzi.mocks

import com.greencopper.thuzi.ThuziAPI
import com.greencopper.thuzi.ThuziRequest
import com.greencopper.thuzi.ThuziResponse
import okhttp3.ResponseBody
import retrofit2.Response

internal class MockThuziAPI(
    var getAttendeeResponse: () -> ThuziResponse.Attendee = { throw NotImplementedError() },
    var getProfileResponse: () -> Map<String, String> = { throw NotImplementedError() },
    var badgesResponse: () -> ThuziResponse.Badges = { throw NotImplementedError() },
    var badgeImageResponse: () -> Response<ResponseBody> = { throw NotImplementedError() },
    var deleteAccountResponse: () -> Unit = { throw NotImplementedError() },
    var checkInResponse: () -> Unit = { throw NotImplementedError() },
    var copyRoutineResponse: () -> ThuziResponse.CopyResponse = { throw NotImplementedError() },
) : ThuziAPI {

    var getAttendeeCount = 0
    var getProfileCount = 0
    var getBadgeImageCount = 0
    var checkInCount = 0

    override suspend fun getAttendee(
        url: String,
        authorizationHeader: String
    ): ThuziResponse.Attendee {
        getAttendeeCount += 1
        return getAttendeeResponse()
    }

    override suspend fun getProfile(
        url: String,
        authorizationHeader: String
    ): Map<String, String> {
        getProfileCount += 1
        return getProfileResponse()
    }

    override suspend fun getBadges(url: String): ThuziResponse.Badges = badgesResponse()

    override suspend fun getBadgeImage(url: String): Response<ResponseBody> {
        getBadgeImageCount += 1
        return badgeImageResponse()
    }

    override suspend fun deleteAccount(url: String, authorizationHeader: String, body: ThuziRequest.AccountDeletion) =
        deleteAccountResponse()

    override suspend fun checkIn(url: String, authorizationHeader: String) {
        checkInCount += 1
        checkInResponse()
    }

    override suspend fun copyRoutine(url: String, authorizationHeader: String): ThuziResponse.CopyResponse = copyRoutineResponse()
}
