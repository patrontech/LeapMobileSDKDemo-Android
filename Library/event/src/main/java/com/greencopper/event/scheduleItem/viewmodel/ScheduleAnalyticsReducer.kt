package com.greencopper.event.scheduleItem.viewmodel

import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.metrics.NextButtonTap
import com.greencopper.event.metrics.schedule
import com.greencopper.event.scheduleItem.ScheduleLayoutData
import com.greencopper.event.scheduleItem.ui.AddMyScheduleAnalytics
import com.greencopper.event.scheduleItem.ui.MyScheduleAnalytics
import com.greencopper.event.scheduleItem.ui.RemoveMyScheduleAnalytics
import com.greencopper.event.scheduleItem.ui.datepicker.DatePickerTap
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.toolkit.App
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withoutEffect
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class ScheduleAnalyticsReducer(
    private val metricsService: AggregateMetricsService,
    private val filteringHandler: FilteringHandler,
    private val timezoneProvider: TimezoneProvider,
    private val scheduleData: ScheduleLayoutData,
) : Reducer<ScheduleState, ScheduleAction> {

    override fun reduce(state: ScheduleState, action: ScheduleAction): ReduceResult<ScheduleState, ScheduleAction> {
        return state.also {
            when (action) {
                is ScheduleAction.User.TappedAddToMySchedule -> {
                    val myScheduleAnalyticsData = MyScheduleAnalytics.Data(
                        scheduleData.analytics.screenName,
                        action.scheduleItemId,
                        action.itemName,
                        action.startDateTime
                    )
                    metricsService.track(AddMyScheduleAnalytics(myScheduleAnalyticsData))
                }

                is ScheduleAction.User.TappedRemoveFromMySchedule -> {
                    val myScheduleAnalyticsData = MyScheduleAnalytics.Data(
                        scheduleData.analytics.screenName,
                        action.scheduleItemId,
                        action.itemName,
                        action.startDateTime
                    )
                    metricsService.track(RemoveMyScheduleAnalytics(myScheduleAnalyticsData))
                }

                is ScheduleAction.User.TappedDay -> {
                    metricsService.track(
                        DatePickerTap(
                            scheduleData.analytics.screenName,
                            action.date.withZoneSameInstant(timezoneProvider.zoneId)
                        )
                    )
                }

                is ScheduleAction.User.TappedNext -> {
                    metricsService.track(
                        NextButtonTap(
                            scheduleData.analytics.screenName,
                            action.date.withZoneSameInstant(timezoneProvider.zoneId)
                        )
                    )
                }

                is ScheduleAction.LoadContent -> {
                    val paramsEvent = mutableMapOf<EventParameter, String>()
                    paramsEvent[EventParameter("view")] = when (it.selectedView) {
                        SelectedView.List -> "list"
                        SelectedView.Timeline -> "timeline"
                    }
                    paramsEvent[EventParameter("favoritesOnly")] = it.isInMySchedule.toString()
                    paramsEvent[EventParameter("interestsOnly")] = it.isInMyInterests.toString()
                    paramsEvent[EventParameter("filteringPredicate")] =
                        filteringHandler.predicate.replayCache.firstOrNull().toString()
                    it.header.datePicker?.selectedDate?.let { selectedDate ->
                        if (selectedDate != ZonedDateTime.ofInstant(Instant.EPOCH, App.zoneId)) {
                            paramsEvent[EventParameter("selectedDate")] =
                                selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        }
                    }
                    metricsService.track(
                        ScreenViewEvent(
                            Screen.schedule(scheduleData.analytics.screenName),
                            paramsEvent
                        )
                    )
                }

                else -> Unit
            }
        }.withoutEffect()
    }

}
