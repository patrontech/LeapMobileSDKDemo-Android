package com.greencopper.event.reminders

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.content.manager.waitForContentApply
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.core.permissions.notification.service.NotificationPermissionService
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.common.EventProjectLocalStorageDomain
import com.greencopper.event.common.event
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.data.repository.TimedScheduleItemRepository
import com.greencopper.event.recipe.EventConfiguration
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.event.reminders.notifications.NotificationWorker
import com.greencopper.event.scheduleItem.ui.utils.minutesToSeconds
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.extensions.formatTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

private const val SCHEDULE_REMINDER_TAG = "tag.scheduleReminders"

internal class ConcreteScheduleRemindersService(
    private val workManager: WorkManager,
    private val scope: CoroutineScope,
    private val currentProjectTagProvider: CurrentProjectTagProvider,
    private val timezoneProvider: TimezoneProvider,
    private val localizationService: LocalizationService,
    private val eventConfigHolder: EventConfigurationHolder,
    private val timedScheduleItemRepository: TimedScheduleItemRepository,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val notificationPermissionService: NotificationPermissionService,
    private val myScheduleManager: FavoritesManager<Long>,
) : ScheduleRemindersService {

    private val eventLocalStorage: EventProjectLocalStorageDomain
        get() = lazyLocalStorage.resolve().project.event

    private var scheduleRemindersJob: Job? = null

    override fun collectScheduleReminders() {
        scope.launch {
            eventConfigHolder.currentConfiguration
                .filterNotNull()
                .waitForContentApply(currentProjectTagProvider)
                .collectLatest { eventConfig ->
                    // restart collecting schedule reminder flows after project changes,
                    // since localStorage needs to be re-resolved
                    scheduleRemindersJob?.cancel()

                    scheduleRemindersJob = scope.launch {
                        combine(
                            eventLocalStorage.reminderIntervalMins.state,
                            myScheduleManager.favoriteIdsFlow,
                        ) { interval, _ ->
                            interval
                        }.collectLatest { interval ->
                            workManager.cancelAllWorkByTag(SCHEDULE_REMINDER_TAG)
                            interval?.let { checkChangeInterval(eventConfig, it) }

                            if (
                                notificationPermissionService.getAuthorizationStatus() is AuthorizationStatus.AuthorizedAlways &&
                                interval != null &&
                                interval >= 0
                            ) {
                                scheduleReminders(interval, workManager)
                            }
                        }
                }
            }
        }
    }

    override fun setReminderInterval(interval: Int) {
        eventLocalStorage.reminderIntervalMins.value = interval
    }

    private fun checkChangeInterval(config: EventConfiguration, interval: Int) {
        val intervals: List<Int> = config.reminders?.timeIntervals
            ?.map { it.value }
            ?.sorted()
            ?: emptyList()

        val newInterval = if (!intervals.contains(interval)) {
            intervals.firstOrNull { it >= interval }
                ?: intervals.lastOrNull()
                ?: -1
        } else {
            interval
        }

        eventLocalStorage.reminderIntervalMins.value = newInterval
    }

    private suspend fun scheduleReminders(interval: Int, workManager: WorkManager) {
        timedScheduleItemRepository
            .getTimedScheduleItemsForScheduleItemIds(eventLocalStorage.myScheduleItemIds.value.toList())
            .first()
            .forEach { scheduleNotificationFor(it, interval, workManager) }
    }

    private fun scheduleNotificationFor(timedScheduleItem: TimedScheduleItem, interval: Int, workManager: WorkManager) {
        val now = ZonedDateTime.now()

        if (timedScheduleItem.timeSlot.startDate == null) return // can't schedule notification for something with no start time
        if (timedScheduleItem.timeSlot.startDate.isBefore(now)) return // don't schedule notifications for times that have passed

        val eventTime = timedScheduleItem.timeSlot.startDate.withZoneSameInstant(timezoneProvider.zoneId)
        val delay = Duration.between(now, eventTime).seconds - interval.minutesToSeconds()

        // Don't schedule notifications that should have happened in the past.
        // A small grace period is added for when the app isn't running. If NotificationWorker is
        // triggered when the app isn't running, startup causes schedule reminders to be
        // re-collected, and the existing worker is cancelled.
        if (delay < -10) return

        val eventTitle = localizationService.getString(timedScheduleItem.scheduleItem.name)
        val intervalData = eventConfigHolder.currentConfiguration.value?.reminders?.timeIntervals
            ?.firstOrNull { it.value == interval }
        val text = localizationService
            .getString(intervalData?.notificationMessage)
            ?.formatTemplate(eventTitle)

        if (text != null) {
            val data = Data.Builder()
                .putString(NotificationWorker.TITLE_KEY, text)
                .putLong(
                    NotificationWorker.SCHEDULE_ITEM_ID_KEY,
                    timedScheduleItem.scheduleItem.itemId
                )
                .build()

            val request = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .setInputData(data)
                .addTag(SCHEDULE_REMINDER_TAG)
                .build()

            workManager.enqueue(request)
        }
    }
}
