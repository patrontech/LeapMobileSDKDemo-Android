package com.greencopper.event.reminders.viewmodel

import android.app.NotificationManager
import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.notification.notificationmanager.NotificationManagerClient
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.core.permissions.SettingsPanelConfig
import com.greencopper.core.permissions.notification.service.NotificationPermissionService
import com.greencopper.event.R
import com.greencopper.event.common.event
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.event.recipe.NO_REMINDERS_INTERVAL
import com.greencopper.event.reminders.ScheduleRemindersService
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.first

internal class RemindersViewModel(
    private val configHolder: EventConfigurationHolder,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val reminderService: ScheduleRemindersService,
    private val notificationPermissionService: NotificationPermissionService,
    private val localizationService: LocalizationService,
    private val notificationManager: NotificationManagerClient,
) : ViewModel() {

    private val eventLocalStorage by lazy { lazyLocalStorage.resolve().project.event }

    fun getIntervals() = configHolder.currentConfiguration.value?.reminders?.timeIntervals ?: emptyList()

    fun getDefaultInterval(context: Context): Int = if (
        areNotificationsEnabled(context) &&
        notificationPermissionService.getAuthorizationStatus() == AuthorizationStatus.AuthorizedAlways
    ) {
        eventLocalStorage.reminderIntervalMins.value
            ?: configHolder.currentConfiguration.value?.reminders?.defaultTimeInterval
            ?: NO_REMINDERS_INTERVAL
    } else {
        NO_REMINDERS_INTERVAL
    }

    fun removeReminders() = reminderService.setReminderInterval(NO_REMINDERS_INTERVAL)

    fun setRemindersInterval(interval: Int) = reminderService.setReminderInterval(interval)

    fun areNotificationsEnabled(context: Context): Boolean {
        return when {
            !notificationManager.areNotificationsEnabled() -> false
            else -> {
                val channelId = context.getString(R.string.default_notification_channel_id)
                val channel = notificationManager.getNotificationChannel(channelId)
                return channel?.importance != NotificationManager.IMPORTANCE_NONE
            }
        }
    }

    fun getNotificationsAuthorizationStatus(): AuthorizationStatus =
        notificationPermissionService.getAuthorizationStatus()

    suspend fun requestNotificationPermission(activity: FragmentActivity): Boolean {
        if (
            !notificationPermissionService.requestPermission(activity).first()
            || !areNotificationsEnabled(activity)
        ) {
            return notificationPermissionService.showSettingsDialog(
                SettingsPanelConfig(
                    title = localizationService.getString("event.schedule.notifications_denied.title"),
                    message = localizationService.getString("event.schedule.notifications_denied.message"),
                    positiveButtonString = localizationService.getString("common.settings"),
                    negativeButtonString = localizationService.getString("common.cancel"),
                    intentToOpen = notificationPermissionService.getSettingsIntent(activity),
                )
            ).first()
        } else {
            return true
        }
    }
}
