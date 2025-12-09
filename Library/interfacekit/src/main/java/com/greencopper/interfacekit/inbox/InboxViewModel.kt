package com.greencopper.interfacekit.inbox

import androidx.lifecycle.ViewModel
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.inbox.InboxViewModel.FetchNotificationsUiState.*
import com.greencopper.interfacekit.inbox.localstorage.inbox
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

internal class InboxViewModel(
    private val inboxNotificationsRepository: InboxNotificationsRepository,
    val localStorage: LocalStorage,
    val timezoneProvider: TimezoneProvider,
    val localizationService: LocalizationService,
    val conditionChecker: ConditionChecker,
    private val scope: CoroutineScope,
) : ViewModel() {

    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

    private val _uiState = MutableSharedFlow<FetchNotificationsUiState>(replay = 1)
    val uiState: SharedFlow<FetchNotificationsUiState> = _uiState

    fun notifications(timezone: ZoneId): Map<ZonedDateTime, List<Notifications.Notification>> =
        localStorage.project.interfaceKit.inbox.offlineItems.value.sort(timezone)

    fun fetchNotifications(url: String) {
        _uiState.tryEmit(Loading)
        scope.launch {
            try {
                val notifications = inboxNotificationsRepository.fetchInboxNotifications(url)
                localStorage.project.interfaceKit.inbox.offlineItems.value = notifications.items.toSet()
                _uiState.tryEmit(Success)
            } catch (t: Throwable) {
                App.log.e(message = "Error when fetching notifications", throwable = t)
                _uiState.tryEmit(Error(t))
            }
        }
    }

    private fun Set<Notifications.Notification>.sort(timezone: ZoneId) =
        sortedByDescending {
            parseDate(it.date, timezone)
        }.groupBy {
            parseDate(it.date, timezone)
                .truncatedTo(ChronoUnit.DAYS)
        }

    private fun parseDate(date: String, timezone: ZoneId) =
        ZonedDateTime.parse(date, dateTimeFormatter).withZoneSameInstant(timezone)

    internal sealed class FetchNotificationsUiState {
        data object Loading : FetchNotificationsUiState()
        data object Success : FetchNotificationsUiState()
        data class Error(val throwable: Throwable) : FetchNotificationsUiState()
    }
}
