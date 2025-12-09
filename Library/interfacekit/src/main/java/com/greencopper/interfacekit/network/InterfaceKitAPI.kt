package com.greencopper.interfacekit.network

import com.greencopper.interfacekit.inbox.Notifications
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

internal interface InterfaceKitAPI {

    @GET
    suspend fun getInboxNotifications(
        @Url url: String,
        @Header("Authorization") authHeader: String,
    ) : Notifications
}
