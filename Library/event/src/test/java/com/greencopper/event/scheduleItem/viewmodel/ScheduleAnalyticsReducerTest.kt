package com.greencopper.event.scheduleItem.viewmodel

import com.greencopper.core.metrics.Metrics
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.event.metrics.NextButtonTap
import com.greencopper.event.metrics.schedule
import com.greencopper.event.scheduleItem.ui.AddMyScheduleAnalytics
import com.greencopper.event.scheduleItem.ui.MyScheduleAnalytics
import com.greencopper.event.scheduleItem.ui.RemoveMyScheduleAnalytics
import com.greencopper.event.scheduleItem.ui.datepicker.DatePickerTap
import com.greencopper.interfacekit.filtering.MockFilteringPredicateComputed
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.testmocks.core.MockTimezoneProvider
import com.greencopper.testmocks.interfacekit.MockFilteringHandler
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import com.toggl.komposable.test.assertNoEffectWereReturned
import com.toggl.komposable.test.testReduce
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class ScheduleAnalyticsReducerTest {

    private val reducer: ScheduleAnalyticsReducer
    private val metricsService = MockAggregateMetricsService()
    private val filteringHandler = MockFilteringHandler()
    private val timezoneProvider = MockTimezoneProvider(ZoneId.of("America/Phoenix"))
    private val scheduleData = createScheduleData(screenName = "analyticsScreenName")
    private var initialState = ScheduleState()

    private val now = ZonedDateTime.of(
        /* year = */ 2023,
        /* month = */ 10,
        /* dayOfMonth = */ 20,
        /* hour = */ 10,
        /* minute = */ 0,
        /* second = */ 0,
        /* nanoOfSecond = */ 0,
        /* zone = */ timezoneProvider.zoneId
    )

    init {
        Toolkit.setupTest()
        mockkStatic(ZonedDateTime::class)
        every { ZonedDateTime.now() } returns now
        every { ZonedDateTime.now(any<ZoneId>()) } returns now

        reducer = ScheduleAnalyticsReducer(
            metricsService = metricsService,
            filteringHandler = filteringHandler,
            timezoneProvider = timezoneProvider,
            scheduleData = scheduleData,
        )
    }

    @AfterEach
    fun afterEach() {
        unmockkAll()
    }

    @Test
    fun tappedAddToMySchedule() {
        val myScheduleAnalyticsData = MyScheduleAnalytics.Data(
            "analyticsScreenName",
            123,
            "itemName123",
            now.plusHours(1)
        )

        testFunction(
            action = ScheduleAction.User.TappedAddToMySchedule(
                scheduleItemId = 123,
                itemName = "itemName123",
                startDateTime = now.plusHours(1)
            ),
            expectedMetrics = AddMyScheduleAnalytics(myScheduleAnalyticsData)
        )
    }

    @Test
    fun tappedRemoveFromMySchedule() {
        val myScheduleAnalyticsData = MyScheduleAnalytics.Data(
            "analyticsScreenName",
            123,
            "itemName123",
            now.plusHours(1)
        )

        testFunction(
            action = ScheduleAction.User.TappedRemoveFromMySchedule(
                scheduleItemId = 123,
                itemName = "itemName123",
                startDateTime = now.plusHours(1)
            ),
            expectedMetrics = RemoveMyScheduleAnalytics(myScheduleAnalyticsData)
        )
    }

    @Test
    fun tappedDay() {
        testFunction(
            action = ScheduleAction.User.TappedDay(
                date = now.plusHours(1).withZoneSameInstant(ZoneId.of("Europe/Paris"))
            ),
            expectedMetrics = DatePickerTap(
                "analyticsScreenName",
                now.plusHours(1)
            )
        )
    }

    @Test
    fun tappedNext() {
        testFunction(
            action = ScheduleAction.User.TappedNext(
                date = now.plusHours(1).withZoneSameInstant(ZoneId.of("Europe/Paris"))
            ),
            expectedMetrics = NextButtonTap(
                "analyticsScreenName",
                now.plusHours(1)
            )
        )
    }

    @Test
    fun loadContent_list_isInMySchedule_isInMyInterests_noSelectedDate() {
        val paramsEvent = mutableMapOf<EventParameter, String>()
        paramsEvent[EventParameter("view")] = "list"
        paramsEvent[EventParameter("favoritesOnly")] = true.toString()
        paramsEvent[EventParameter("interestsOnly")] = true.toString()
        paramsEvent[EventParameter("filteringPredicate")] = "predicate456"

        initialState = initialState.copy(
            selectedView = SelectedView.List,
            selectedSchedule = setOf(SelectedSchedule.MySchedule, SelectedSchedule.MyInterests)
        )
        filteringHandler.mockedPredicate = MockFilteringPredicateComputed(queryPattern = "predicate456")

        testFunction(
            action = ScheduleAction.LoadContent(purge = false),
            expectedMetrics = ScreenViewEvent(
                Screen.schedule("analyticsScreenName"),
                paramsEvent
            )
        )
    }

    @Test
    fun loadContent_timeline_isNotInMySchedule_selectedDate() {
        val paramsEvent = mutableMapOf<EventParameter, String>()
        paramsEvent[EventParameter("view")] = "timeline"
        paramsEvent[EventParameter("favoritesOnly")] = false.toString()
        paramsEvent[EventParameter("interestsOnly")] = false.toString()
        paramsEvent[EventParameter("filteringPredicate")] = "null"
        paramsEvent[EventParameter("selectedDate")] = now.plusWeeks(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

        initialState = initialState.copy(
            selectedView = SelectedView.Timeline,
            selectedSchedule = emptySet(),
            header = ViewState.HeaderState(
                datePicker = ViewState.DatePickerState(
                    selectedDate = now.plusWeeks(1),
                    displayMode = DisplayMode.DAILY
                )
            )
        )
        filteringHandler.mockedPredicate = null

        testFunction(
            action = ScheduleAction.LoadContent(purge = false),
            expectedMetrics = ScreenViewEvent(
                Screen.schedule("analyticsScreenName"),
                paramsEvent
            )
        )
    }

    private fun testFunction(action: ScheduleAction, expectedMetrics: Metrics) = runTest {
        reducer.testReduce(initialState, action) { state, effect ->

            assertThat(metricsService.trackedMetrics).hasSize(1)
            assertThat(metricsService.trackedMetrics.first()).usingRecursiveComparison().isEqualTo(expectedMetrics)

            assertNoEffectWereReturned(state, effect)
        }
    }

}
