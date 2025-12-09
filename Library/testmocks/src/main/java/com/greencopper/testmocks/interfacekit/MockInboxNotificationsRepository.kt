package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.inbox.InboxNotificationsRepository
import com.greencopper.interfacekit.inbox.Notifications

public class MockInboxNotificationsRepository(
    private val notifications: () -> Notifications = { Notifications(emptyList()) }
) :
    InboxNotificationsRepository {
    public var fetchNotificationsCount: Int = 0

    override suspend fun fetchInboxNotifications(url: String): Notifications =
        notifications().also { fetchNotificationsCount++ }
}
