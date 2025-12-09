package com.greencopper.core.notification.notificationmanager

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.content.Context
import androidx.core.app.NotificationManagerCompat

public interface NotificationManagerClient {
    public fun areNotificationsEnabled(): Boolean
    public fun notify(id: Int, notification: Notification)
    public fun getNotificationChannel(channelId: String): NotificationChannel?
    public fun cancel(id: Int)
}

@SuppressLint("MissingPermission")
internal class ConcreteNotificationManagerClient(context: Context) : NotificationManagerClient {

    val notifManager = NotificationManagerCompat.from(context)

    override fun areNotificationsEnabled(): Boolean = notifManager.areNotificationsEnabled()

    override fun notify(id: Int, notification: Notification) {
        notifManager.notify(id, notification)
    }

    override fun getNotificationChannel(channelId: String): NotificationChannel? =
        notifManager.getNotificationChannel(channelId)

    override fun cancel(id: Int) {
        notifManager.cancel(id)
    }
}
