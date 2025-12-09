package com.greencopper.interfacekit.mocks

import com.greencopper.interfacekit.inbox.Notifications
import com.greencopper.interfacekit.network.InterfaceKitAPI

internal class MockInterfaceKitAPI(
    var notificationsResponse: () -> Notifications = { throw NotImplementedError() }
) : InterfaceKitAPI {

    override suspend fun getInboxNotifications(url: String, authHeader: String): Notifications = notificationsResponse()
}
