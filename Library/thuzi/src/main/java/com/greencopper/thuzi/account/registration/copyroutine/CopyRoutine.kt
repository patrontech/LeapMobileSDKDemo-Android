package com.greencopper.thuzi.account.registration.copyroutine

public interface CopyRoutine {
    public suspend fun getNewJwt(
        url: String,
        brandId: String,
        eventId: String,
    ): String?
}