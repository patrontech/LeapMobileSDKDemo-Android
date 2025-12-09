package com.greencopper.thuzi.mocks

import com.greencopper.thuzi.account.registration.copyroutine.CopyRoutine

internal class MockCopyRoutine(
    var newToken: String? = "",
    var exception: Exception? = null,
): CopyRoutine {
    override suspend fun getNewJwt(url: String, brandId: String, eventId: String): String? {
        exception?.let { throw it }
        return newToken
    }
}
