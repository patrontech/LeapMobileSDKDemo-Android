package com.greencopper.coremocks

import android.app.Notification
import android.app.NotificationChannel
import com.greencopper.core.notification.notificationmanager.NotificationManagerClient

public class MockNotificationManagerClient(
    public var areNotificationsEnabledAction: () -> Boolean = { throw NotImplementedError() },
    public var notifyAction: (Int, Notification) -> Unit = { _, _ -> throw NotImplementedError() },
    public var getNotificationChannelAction: (String) -> NotificationChannel? = { throw NotImplementedError() },
    public var cancelAction: (Int) -> Unit = { throw NotImplementedError() },
) : NotificationManagerClient {

    override fun areNotificationsEnabled(): Boolean = areNotificationsEnabledAction()
    override fun notify(id: Int, notification: Notification): Unit = notifyAction(id, notification)
    override fun getNotificationChannel(channelId: String): NotificationChannel? = getNotificationChannelAction(channelId)
    override fun cancel(id: Int): Unit = cancelAction(id)
}
