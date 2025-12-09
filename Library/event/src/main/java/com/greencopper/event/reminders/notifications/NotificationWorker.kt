package com.greencopper.event.reminders.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.greencopper.core.notification.notificationmanager.NotificationManagerClient
import com.greencopper.event.R
import com.greencopper.event.recipe.EventConfiguration
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.ui.activity.KibaMainActivity
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import com.greencopper.toolkit.di.resolver.resolve

internal class NotificationWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        internal const val TITLE_KEY = "key.title"
        internal const val SCHEDULE_ITEM_ID_KEY = "key.scheduleItemId"
    }

    private val eventConfig: EventConfiguration?
        get() = App.resolve<EventConfigurationHolder>().currentConfiguration.value
    private val notificationManager: NotificationManagerClient
        get() = App.resolve()
    private val linkResolver: LinkResolver by App.lazy()

    override fun doWork(): Result {
        val channelId = context.getString(R.string.default_notification_channel_id)
        val builder = Notification.Builder(context, channelId)

        val itemId = inputData.getLong(SCHEDULE_ITEM_ID_KEY, 0L)
        val uri = eventConfig?.reminders?.onNotificationTapRouteLink?.let {
            buildUri(it, itemId)
        }
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            buildOnTap(this, uri)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            itemId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        builder
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle(inputData.getString(TITLE_KEY))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(itemId.toInt(), builder.build())

        return Result.success()
    }

    @VisibleForTesting
    internal fun buildOnTap(intent: Intent, uri: Uri?) {
        intent.putExtra(KibaMainActivity.INTENT_KEY_ON_TAP, uri?.toString())
    }

    private fun buildUri(routeLink: String, scheduleItemId: Long): Uri = linkResolver.routeUri(
        routeLink,
        mapOf("scheduleItemId" to scheduleItemId.toString())
    )
}
