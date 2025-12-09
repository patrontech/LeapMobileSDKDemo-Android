package com.greencopper.event.scheduleItem.viewmodel

import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.scheduleItem.ScheduleLayoutData
import com.greencopper.event.scheduleItem.ui.timeline.TimelineAdapter
import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.interests.integration.IntegratedInterestsData
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.core.MockTimezoneProvider
import com.greencopper.testmocks.interfacekit.MockWidgetParameters
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.extensions.getFormattedDateTime
import com.greencopper.toolkit.extensions.truncateToMonth
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.serialization.json.JsonArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.FormatStyle

private class ScheduleBusinessTest {

    private val timezoneProvider = MockTimezoneProvider(ZoneId.of("America/Phoenix"))

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
    private val pastDay = now.minusDays(2)
    private val pastWeek = now.minusWeeks(1)
    private val pastMonth = now.minusMonths(1)
    private val futureDay = now.plusDays(2)
    private val futureWeek = now.plusWeeks(1)
    private val futureMonth = now.plusMonths(1)

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        bindSingleton<TimezoneProvider>(timezoneProvider)

        mockkStatic(ZonedDateTime::class)
        every { ZonedDateTime.now() } returns now
        every { ZonedDateTime.now(any<ZoneId>()) } returns now
    }

    @Nested
    @DisplayName("loadDatePicker")
    inner class InitialLoadDatePicker {

        @Test
        fun noDates_shouldReturnEmpty() {
            testFunction(
                displayMode = DisplayMode.DAILY,
                selectedSchedule = setOf(SelectedSchedule.MyInterests),
                initialItems = emptyList(),
                initialSelectedDate = null,
                expectedDates = emptyList(),
                expectedSelectedDate = now
            )
        }

        @Test
        fun noDates_withSelectedDate_shouldReturnEmpty_andKeepSelectedDate() {
            testFunction(
                displayMode = DisplayMode.DAILY,
                selectedSchedule = setOf(SelectedSchedule.MyInterests),
                initialItems = emptyList(),
                initialSelectedDate = pastDay,
                expectedDates = emptyList(),
                expectedSelectedDate = pastDay
            )
        }

        @Test
        fun dailyMode_withNoSelectedDate_shouldDistinctAndSort_selectFutureDateNearest() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
                TimedScheduleItemGeneratorItem(eventDate = futureMonth),
                TimedScheduleItemGeneratorItem(eventDate = pastWeek),
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
            )

            testFunction(
                displayMode = DisplayMode.DAILY,
                selectedSchedule = setOf(SelectedSchedule.MyInterests),
                initialItems = generatedItems,
                initialSelectedDate = null,
                expectedDates = listOf(pastWeek, pastDay, futureMonth),
                expectedSelectedDate = futureMonth
            )
        }

        @Test
        fun withSelectedDate_selectedDateInList_keepSelectedDate() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = pastMonth),
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
                TimedScheduleItemGeneratorItem(eventDate = futureDay),
            )

            testFunction(
                displayMode = DisplayMode.DAILY,
                selectedSchedule = setOf(SelectedSchedule.MyInterests),
                initialItems = generatedItems,
                initialSelectedDate = pastMonth,
                expectedDates = listOf(pastMonth, pastDay, futureDay),
                expectedSelectedDate = pastMonth
            )
        }

        @Test
        fun withSelectedDate_selectedDateNotInList_selectDateNearestToSelectedDate() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = pastWeek),
                TimedScheduleItemGeneratorItem(eventDate = futureMonth),
            )

            testFunction(
                displayMode = DisplayMode.DAILY,
                selectedSchedule = setOf(SelectedSchedule.MyInterests),
                initialItems = generatedItems,
                initialSelectedDate = pastDay,
                expectedDates = listOf(pastWeek, futureMonth),
                expectedSelectedDate = pastWeek
            )
        }

        @Test
        fun monthly_withNoSelectedDate_shouldTruncateDates_selectFutureDateNearest() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = futureMonth.minusDays(1)),
                TimedScheduleItemGeneratorItem(eventDate = futureMonth.plusMonths(1)),
                TimedScheduleItemGeneratorItem(eventDate = futureMonth.plusWeeks(1)),
            )

            testFunction(
                displayMode = DisplayMode.MONTHLY,
                selectedSchedule = setOf(SelectedSchedule.MyInterests),
                initialItems = generatedItems,
                initialSelectedDate = null,
                expectedDates = listOf(futureMonth.truncateToMonth(), futureMonth.plusMonths(1).truncateToMonth()),
                expectedSelectedDate = futureMonth.truncateToMonth()
            )
        }

        @Test
        fun monthly_withSelectedDate_selectedDateInList_keepSelectedDate() {
            val initialSelectedDate = futureMonth.truncateToMonth()

            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = initialSelectedDate.minusDays(1)),
                TimedScheduleItemGeneratorItem(eventDate = initialSelectedDate.plusWeeks(1)),
            )

            testFunction(
                displayMode = DisplayMode.MONTHLY,
                selectedSchedule = setOf(SelectedSchedule.MyInterests),
                initialItems = generatedItems,
                initialSelectedDate = futureMonth.truncateToMonth(),
                expectedDates = listOf(now.truncateToMonth(), futureMonth.truncateToMonth()),
                expectedSelectedDate = futureMonth.truncateToMonth()
            )
        }

        @Test
        fun monthly_onlyOneItemResult_shouldReturnEmpty() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = futureMonth.minusDays(1)),
                TimedScheduleItemGeneratorItem(eventDate = futureMonth.plusWeeks(1)),
            )

            testFunction(
                displayMode = DisplayMode.MONTHLY,
                selectedSchedule = setOf(SelectedSchedule.MyInterests),
                initialItems = generatedItems,
                initialSelectedDate = null,
                expectedDates = emptyList(),
                expectedSelectedDate = futureMonth.truncateToMonth()
            )
        }

        @Test
        fun daily_shouldHideDatePicker_shouldReturnEmpty() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
                TimedScheduleItemGeneratorItem(eventDate = futureMonth),
                TimedScheduleItemGeneratorItem(eventDate = pastWeek),
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
            )

            testFunction(
                displayMode = DisplayMode.DAILY,
                selectedSchedule = setOf(SelectedSchedule.MySchedule),
                showPicker = false,
                initialItems = generatedItems,
                initialSelectedDate = null,
                expectedDates = emptyList(),
                expectedSelectedDate = futureMonth
            )
        }

        fun testFunction(
            displayMode: DisplayMode,
            selectedSchedule: Set<SelectedSchedule>,
            showPicker: Boolean = true,
            initialItems: List<TimedScheduleItem>,
            initialSelectedDate: ZonedDateTime?,
            expectedDates: List<ZonedDateTime>,
            expectedSelectedDate: ZonedDateTime,
        ) {
            val scheduleData =
                createScheduleData(
                    displayMode = displayMode,
                    myFavorites = FavoriteConfig(
                        showPicker = showPicker,
                        emptyPage = EmptyPage("emptyTitle", "emptySubtitle", "emptyImage")
                    )
                )
            val initialState = ScheduleState(items = initialItems, selectedSchedule = selectedSchedule).let {
                if (initialSelectedDate != null)
                    it.copy(
                        header = it.header.copy(
                            datePicker = ViewState.DatePickerState(
                                dates = emptyList(),
                                selectedDate = initialSelectedDate,
                                displayMode = scheduleData.displayMode
                            )
                        )
                    )
                else it
            }

            val resultState = loadDatePicker(initialState, scheduleData)

            val expectedState = initialState.copy(
                header = initialState.header.copy(
                    datePicker = ViewState.DatePickerState(
                        dates = expectedDates,
                        selectedDate = expectedSelectedDate,
                        displayMode = scheduleData.displayMode
                    )
                )
            )

            assertThat(resultState).isEqualTo(expectedState)
        }
    }

    @Nested
    @DisplayName("loadContent List")
    inner class InitialLoadContent_List {

        var initialData = createScheduleData()
        var initialState = ScheduleState(
            selectedView = SelectedView.List,
            header = ViewState.HeaderState(
                datePicker = ViewState.DatePickerState(
                    dates = emptyList(),
                    selectedDate = now,
                    displayMode = initialData.displayMode
                )
            )
        )

        private val localizationService = MockLocalizationService()

        @Test
        fun ifNoPurge_andStateNotSet_shouldCreateState_withoutClearingTimeline() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(now),
            )
            val favoriteIds = emptySet<Long>()

            initialState = initialState.copy(
                list = null,
                timeline = ViewState.TimelineState.Empty("a", "b", "c", null, "screenName"),
            )

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(now),
                selectedDate = now,
                purge = false,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                expectedList = ViewState.ListState.Content(
                    items = listOf(
                        generatedItems[0].toScheduleListViewData(favoriteIds),
                    )
                ),
                expectedTimeline = ViewState.TimelineState.Empty("a", "b", "c", null, "screenName"),
            )
        }

        @Test
        fun ifPurge_andStateAlreadySet_shouldRecreateState_andClearTimeline() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(now),
            )
            val favoriteIds = emptySet<Long>()

            initialState = initialState.copy(
                list = null,
                timeline = ViewState.TimelineState.Empty("", "", "", null, "screenName"),
            )

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(now),
                selectedDate = now,
                purge = true,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                expectedList = ViewState.ListState.Content(
                    items = listOf(
                        generatedItems[0].toScheduleListViewData(favoriteIds),
                    )
                ),
                expectedTimeline = null,
            )
        }

        @Test
        fun ifNoPurge_andStateAlreadySet_shouldJustReturn() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(now),
            )
            val favoriteIds = emptySet<Long>()

            initialState = initialState.copy(
                list = ViewState.ListState.Empty("a", "b", "c", null, "screenName"),
                timeline = ViewState.TimelineState.Empty("a", "b", "c", null, "screenName"),
            )

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(now),
                selectedDate = now,
                purge = false,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                expectedList = ViewState.ListState.Empty("a", "b", "c", null, "screenName"),
                expectedTimeline = ViewState.TimelineState.Empty("a", "b", "c", null, "screenName"),
            )
        }

        @Test
        fun ifHideDatePicker_shouldShowAllItems() {
            initialData = initialData.copy(
                myFavorites = FavoriteConfig(
                    showPicker = false,
                    emptyPage = EmptyPage("emptyTitle", "emptySubtitle", "emptyImage")
                )
            )
            initialState = initialState.copy(selectedSchedule = setOf(SelectedSchedule.MySchedule))

            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(pastWeek),
                TimedScheduleItemGeneratorItem(now, startDate = now.plusHours(3), hasStage = true),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(2), name = "b"),
                TimedScheduleItemGeneratorItem(futureWeek, startDate = futureWeek, hasStage = true, name = "a"),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(1)),
                TimedScheduleItemGeneratorItem(futureMonth),
            )
            val favoriteIds = setOf<Long>(2, 4)

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(),
                selectedDate = pastDay.truncateToMonth(),
                purge = false,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                expectedList = ViewState.ListState.Content(
                    items = listOf(
                        makeDayHeader(generatedItems[0]),
                        generatedItems[0].toScheduleListViewData(favoriteIds),
                        makeDayHeader(generatedItems[2]),
                        generatedItems[4].toScheduleListViewData(favoriteIds),
                        generatedItems[2].toScheduleListViewData(favoriteIds),
                        makeDayHeader(generatedItems[1]),
                        generatedItems[1].toScheduleListViewData(favoriteIds),
                        makeDayHeader(generatedItems[3]),
                        generatedItems[3].toScheduleListViewData(favoriteIds),
                        makeDayHeader(generatedItems[5]),
                        generatedItems[5].toScheduleListViewData(favoriteIds),
                    )
                ),
                expectedTimeline = null
            )
        }

        @Test
        fun daily_shouldShowSelectedDateItems_sortedByDateAndName() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(pastWeek),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(2), name = "b"),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(3), hasStage = true),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(2), hasStage = true, name = "a"),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(2), name = "a"),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(1)),
            )
            val favoriteIds = setOf<Long>(2, 4)

            val expectedWidgetCollectionHolder = ScheduleListViewData.WidgetCollectionHolder(
                key = 2,
                widgets = listOf(
                    WidgetCollectionView.WidgetItem(
                        key = WidgetCollectionConfiguration.Instance.WidgetKey(
                            name = "testKey",
                            version = 1
                        ),
                        params = MockWidgetParameters("test123")
                    )
                )
            )

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(pastWeek, pastDay),
                selectedDate = pastDay,
                purge = false,
                favoriteIds = favoriteIds,
                widgetCollections = linkedMapOf(
                    2 to listOf(
                        WidgetCollectionView.WidgetItem(
                            WidgetCollectionConfiguration.Instance.WidgetKey(
                                name = "testKey",
                                version = 1
                            ), MockWidgetParameters("test123")
                        )
                    )
                ),
                expectedList = ViewState.ListState.Content(
                    items = listOf(
                        makeTimeHeader(generatedItems[5]),
                        generatedItems[5].toScheduleListViewData(favoriteIds),
                        makeTimeHeader(generatedItems[3]),
                        generatedItems[3].toScheduleListViewData(favoriteIds),
                        expectedWidgetCollectionHolder,
                        generatedItems[4].toScheduleListViewData(favoriteIds),
                        generatedItems[1].toScheduleListViewData(favoriteIds),
                        makeTimeHeader(generatedItems[2]),
                        generatedItems[2].toScheduleListViewData(favoriteIds),
                    )
                ),
                expectedTimeline = null
            )
        }

        @Test
        fun daily_itemsWithoutStartDate_showsFirst() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(pastWeek),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(2), name = "b"),
                TimedScheduleItemGeneratorItem(pastDay, name = "b"),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(3), hasStage = true),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(2), hasStage = true, name = "a"),
                TimedScheduleItemGeneratorItem(pastDay, name = "a"),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(2), name = "a"),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(1)),
            )
            val favoriteIds = setOf<Long>(2, 4)

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(pastWeek, pastDay),
                selectedDate = pastDay,
                purge = false,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                expectedList = ViewState.ListState.Content(
                    items = listOf(
                        generatedItems[5].toScheduleListViewData(favoriteIds),
                        generatedItems[2].toScheduleListViewData(favoriteIds),
                        makeTimeHeader(generatedItems[7]),
                        generatedItems[7].toScheduleListViewData(favoriteIds),
                        makeTimeHeader(generatedItems[4]),
                        generatedItems[4].toScheduleListViewData(favoriteIds),
                        generatedItems[6].toScheduleListViewData(favoriteIds),
                        generatedItems[1].toScheduleListViewData(favoriteIds),
                        makeTimeHeader(generatedItems[3]),
                        generatedItems[3].toScheduleListViewData(favoriteIds),
                    )
                ),
                expectedTimeline = null
            )
        }

        @Test
        fun daily_nextDateAvailable_shouldAddNextButton() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(pastWeek),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(1), name = "b"),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(1), name = "a"),
                TimedScheduleItemGeneratorItem(futureWeek),
            )
            val favoriteIds = setOf<Long>(2, 4)

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(pastWeek, pastDay, futureWeek),
                selectedDate = pastDay,
                purge = false,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                expectedList = ViewState.ListState.Content(
                    items = listOf(
                        makeTimeHeader(generatedItems[1]),
                        generatedItems[2].toScheduleListViewData(favoriteIds),
                        generatedItems[1].toScheduleListViewData(favoriteIds),
                        makeNextButton("event.schedule.next.day", futureWeek)
                    )
                ),
                expectedTimeline = null
            )
        }

        @Test
        fun monthly_shouldShowSelectedDateItems_sortedByDateAndName() {
            initialData = initialData.copy(displayMode = DisplayMode.MONTHLY)
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(pastWeek),
                TimedScheduleItemGeneratorItem(now, startDate = now.plusHours(3), hasStage = true),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(2), name = "b"),
                TimedScheduleItemGeneratorItem(futureWeek, startDate = futureWeek, hasStage = true, name = "a"),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(1)),
            )
            val favoriteIds = setOf<Long>(2, 4)

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(pastDay.truncateToMonth()),
                selectedDate = pastDay.truncateToMonth(),
                purge = false,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                expectedList = ViewState.ListState.Content(
                    items = listOf(
                        makeDayHeader(generatedItems[0]),
                        generatedItems[0].toScheduleListViewData(favoriteIds),
                        makeDayHeader(generatedItems[2]),
                        generatedItems[4].toScheduleListViewData(favoriteIds),
                        generatedItems[2].toScheduleListViewData(favoriteIds),
                        makeDayHeader(generatedItems[1]),
                        generatedItems[1].toScheduleListViewData(favoriteIds),
                        makeDayHeader(generatedItems[3]),
                        generatedItems[3].toScheduleListViewData(favoriteIds),
                    )
                ),
                expectedTimeline = null
            )
        }

        @Test
        fun monthly_nextDateAvailable_shouldAddNextButton() {
            initialData = initialData.copy(displayMode = DisplayMode.MONTHLY)
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(pastWeek),
                TimedScheduleItemGeneratorItem(now, startDate = now.plusHours(3), hasStage = true),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(2), name = "b"),
                TimedScheduleItemGeneratorItem(futureMonth),
                TimedScheduleItemGeneratorItem(futureWeek, startDate = futureWeek, hasStage = true, name = "a"),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(1)),
            )
            val favoriteIds = setOf<Long>(2, 4)

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(pastDay.truncateToMonth(), futureMonth.truncateToMonth()),
                selectedDate = pastDay.truncateToMonth(),
                purge = false,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                expectedList = ViewState.ListState.Content(
                    items = listOf(
                        makeDayHeader(generatedItems[0]),
                        generatedItems[0].toScheduleListViewData(favoriteIds),
                        makeDayHeader(generatedItems[2]),
                        generatedItems[5].toScheduleListViewData(favoriteIds),
                        generatedItems[2].toScheduleListViewData(favoriteIds),
                        makeDayHeader(generatedItems[1]),
                        generatedItems[1].toScheduleListViewData(favoriteIds),
                        makeDayHeader(generatedItems[4]),
                        generatedItems[4].toScheduleListViewData(favoriteIds),
                        makeNextButton("event.schedule.next.month", futureMonth.truncateToMonth())
                    )
                ),
                expectedTimeline = null
            )
        }

        @Test
        fun ifDatePickerNotSetYet_shouldReturnStateUnmodified() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(now),
            )
            val favoriteIds = emptySet<Long>()

            initialState = initialState.copy(
                header = initialState.header.copy(datePicker = null),
                list = ViewState.ListState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
                timeline = ViewState.TimelineState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
            )

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(now),
                selectedDate = now,
                purge = true,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                expectedList = ViewState.ListState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
                expectedTimeline = ViewState.TimelineState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
            )
        }

        @Test
        fun noItemsAvailable_fullSchedule_shouldReturnEmptyState() {
            initialState = initialState.copy(selectedSchedule = emptySet())
            initialData = initialData.copy(emptyScheduleImage = "emptyImage123")
            val favoriteIds = emptySet<Long>()

            testFunction(
                initialItems = emptyList(),
                datePickerDates = emptyList(),
                selectedDate = pastDay,
                purge = false,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                expectedList = ViewState.ListState.Empty(
                    "event.schedule.fullschedule.empty.title",
                    "event.schedule.fullschedule.empty.subtitle",
                    initialData.emptyScheduleImage,
                    null,
                    "screenName"
                ),
                expectedTimeline = null,
            )
        }

        @Test
        fun noItemsAvailable_mySchedule_shouldReturnEmptyState() {
            initialState = initialState.copy(selectedSchedule = setOf(SelectedSchedule.MySchedule))
            initialData = initialData.copy(
                emptyScheduleImage = "emptyImage123",
                myFavorites = FavoriteConfig(
                    emptyPage = EmptyPage("myFavoritesImage", "myFavoritesTitle", "myFavoritesSubtitle")
                )
            )
            val favoriteIds = emptySet<Long>()

            testFunction(
                initialItems = emptyList(),
                datePickerDates = emptyList(),
                selectedDate = pastDay,
                purge = false,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                expectedList = ViewState.ListState.Empty(
                    "myFavoritesTitle",
                    "myFavoritesSubtitle",
                    "myFavoritesImage",
                    null,
                    "screenName"
                ),
                expectedTimeline = null,
            )
        }

        fun testFunction(
            initialItems: List<TimedScheduleItem>,
            datePickerDates: List<ZonedDateTime>,
            selectedDate: ZonedDateTime,
            purge: Boolean,
            favoriteIds: Set<Long> = emptySet(),
            widgetCollections: LinkedHashMap<Int, List<WidgetCollectionView.WidgetItem>>? = null,
            expectedList: ViewState.ListState?,
            expectedTimeline: ViewState.TimelineState?,
        ) {
            val initialState = initialState.copy(
                items = initialItems,
                header = initialState.header.copy(
                    datePicker = initialState.header.datePicker?.copy(
                        dates = datePickerDates,
                        selectedDate = selectedDate,
                    )
                )
            )

            val resultState = loadContent(
                state = initialState,
                purge = purge,
                scheduleData = initialData,
                favoriteIds = favoriteIds,
                widgetCollections = widgetCollections,
                timezoneProvider = timezoneProvider,
                localizationService = localizationService,
                hasInterests = false,
            )

            assertThat(resultState).isEqualTo(
                initialState.copy(
                    list = expectedList,
                    timeline = expectedTimeline,
                )
            )
        }
    }

    @Nested
    @DisplayName("loadContent Timeline")
    inner class InitialLoadContent_Timeline {

        var initialData = createScheduleData(
            timeline = TimelineData(
                displayToggle = false,
                defaultDuration = 45,
                preferredTimeToWidthRatio = 0,
                buttonIcon = "",
                emptyStateImage = "",
            ),
            hideEndTime = false
        )
        var initialState = ScheduleState(
            selectedView = SelectedView.Timeline,
            header = ViewState.HeaderState(
                datePicker = ViewState.DatePickerState(
                    dates = emptyList(),
                    selectedDate = now,
                    displayMode = initialData.displayMode
                )
            )
        )

        private val localizationService = MockLocalizationService()

        @Test
        fun daily_shouldShowSelectedDateItems_withoutNoStartDate_sortedByDateAndName() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(pastWeek),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(2), name = "b"),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(3), hasStage = true),
                TimedScheduleItemGeneratorItem(pastDay),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(2), hasStage = true, name = "a"),
                TimedScheduleItemGeneratorItem(
                    pastDay,
                    startDate = pastDay.plusHours(2),
                    endDate = pastDay.plusHours(3),
                    name = "a"
                ),
                TimedScheduleItemGeneratorItem(pastDay),
                TimedScheduleItemGeneratorItem(pastDay, startDate = pastDay.plusHours(1)),
            )
            val favoriteIds = setOf<Long>(2, 4)

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(pastWeek, pastDay),
                selectedDate = pastDay,
                purge = false,
                favoriteIds = favoriteIds,
                expectedList = null,
                expectedTimeline = ViewState.TimelineState.Content(
                    items = listOf(
                        generatedItems[7].toTimelineAdapterEventData(
                            favoriteIds,
                            pastDay.plusHours(1).plusMinutes(45),
                            false
                        ),
                        generatedItems[4].toTimelineAdapterEventData(
                            favoriteIds,
                            pastDay.plusHours(2).plusMinutes(45),
                            false
                        ),
                        generatedItems[5].toTimelineAdapterEventData(favoriteIds, pastDay.plusHours(3), false),
                        generatedItems[1].toTimelineAdapterEventData(
                            favoriteIds,
                            pastDay.plusHours(2).plusMinutes(45),
                            false
                        ),
                        generatedItems[2].toTimelineAdapterEventData(
                            favoriteIds,
                            pastDay.plusHours(3).plusMinutes(45),
                            false
                        ),
                    )
                )
            )
        }

        @Test
        fun ifNoPurge_andStateNotSet_shouldCreateState_withoutClearingList() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(now, startDate = now),
            )
            val favoriteIds = emptySet<Long>()

            initialState = initialState.copy(
                list = ViewState.ListState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
                timeline = null,
            )

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(now),
                selectedDate = now,
                purge = false,
                favoriteIds = favoriteIds,
                expectedList = ViewState.ListState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
                expectedTimeline = ViewState.TimelineState.Content(
                    items = listOf(
                        generatedItems[0].toTimelineAdapterEventData(favoriteIds, now.plusMinutes(45), false),
                    )
                ),
            )
        }

        @Test
        fun ifPurge_andStateAlreadySet_shouldRecreateState_andClearList() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(now, startDate = now),
            )
            val favoriteIds = emptySet<Long>()

            initialState = initialState.copy(
                list = ViewState.ListState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
                timeline = null,
            )

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(now),
                selectedDate = now,
                purge = true,
                favoriteIds = favoriteIds,
                expectedList = null,
                expectedTimeline = ViewState.TimelineState.Content(
                    items = listOf(
                        generatedItems[0].toTimelineAdapterEventData(favoriteIds, now.plusMinutes(45), false),
                    )
                )
            )
        }

        @Test
        fun ifNoPurge_andStateAlreadySet_shouldJustReturn() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(now, startDate = now),
            )
            val favoriteIds = emptySet<Long>()

            initialState = initialState.copy(
                list = ViewState.ListState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
                timeline = ViewState.TimelineState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
            )

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(now),
                selectedDate = now,
                purge = false,
                favoriteIds = favoriteIds,
                expectedList = ViewState.ListState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
                expectedTimeline = ViewState.TimelineState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
            )
        }

        @Test
        fun ifHideEndDate_endDateShouldUseDefaultParams() {
            initialData = initialData.copy(hideEndTime = true)

            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(now, startDate = now, endDate = now.plusHours(1)),
            )
            val favoriteIds = emptySet<Long>()

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(now),
                selectedDate = now,
                purge = false,
                favoriteIds = favoriteIds,
                expectedList = null,
                expectedTimeline = ViewState.TimelineState.Content(
                    items = listOf(
                        generatedItems[0].toTimelineAdapterEventData(favoriteIds, now.plusMinutes(45), true),
                    )
                ),
            )
        }

        @Test
        fun ifEndDateBelowMinimumDuration_endDateShouldUseDefaultParams() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(now, startDate = now, endDate = now.plusMinutes(5)),
            )
            val favoriteIds = emptySet<Long>()

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(now),
                selectedDate = now,
                purge = false,
                favoriteIds = favoriteIds,
                expectedList = null,
                expectedTimeline = ViewState.TimelineState.Content(
                    items = listOf(
                        generatedItems[0].toTimelineAdapterEventData(favoriteIds, now.plusMinutes(30), false),
                    )
                ),
            )
        }

        @Test
        fun ifDefaultDurationBelowMinimumDuration_endDateShouldUseDefaultParams() {
            initialData = initialData.copy(timeline = initialData.timeline?.copy(defaultDuration = 15))

            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(now, startDate = now),
            )
            val favoriteIds = emptySet<Long>()

            testFunction(
                initialItems = generatedItems,
                datePickerDates = listOf(now),
                selectedDate = now,
                purge = false,
                favoriteIds = favoriteIds,
                expectedList = null,
                expectedTimeline = ViewState.TimelineState.Content(
                    items = listOf(
                        generatedItems[0].toTimelineAdapterEventData(favoriteIds, now.plusMinutes(30), false),
                    )
                ),
            )
        }

        fun testFunction(
            initialItems: List<TimedScheduleItem>,
            datePickerDates: List<ZonedDateTime>,
            selectedDate: ZonedDateTime,
            purge: Boolean,
            favoriteIds: Set<Long> = emptySet(),
            expectedList: ViewState.ListState?,
            expectedTimeline: ViewState.TimelineState?,
        ) {
            val initialState = initialState.copy(
                items = initialItems,
                header = initialState.header.copy(
                    datePicker = initialState.header.datePicker?.copy(
                        dates = datePickerDates,
                        selectedDate = selectedDate,
                    )
                )
            )

            val resultState = loadContent(
                state = initialState,
                purge = purge,
                scheduleData = initialData,
                favoriteIds = favoriteIds,
                widgetCollections = null,
                timezoneProvider = timezoneProvider,
                localizationService = localizationService,
                hasInterests = false,
            )

            assertThat(resultState).isEqualTo(
                initialState.copy(
                    list = expectedList,
                    timeline = expectedTimeline,
                )
            )
        }
    }

    @Nested
    @DisplayName("update MySchedule items")
    inner class UpdateMyScheduleItems {

        @Test
        fun contentNotSet_shouldNotChange() {
            val initialState = ScheduleState(
                list = ViewState.ListState.Empty(
                    "a", "b", "c",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                ),
                timeline = null,
            )

            val resultState = updateMyScheduleItems(initialState, setOf(1, 2, 3))

            assertThat(resultState).isEqualTo(initialState)
        }

        @Test
        fun contentSet_shouldEditItems() {

            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(now, startDate = now),
                TimedScheduleItemGeneratorItem(futureDay, startDate = futureDay),
                TimedScheduleItemGeneratorItem(futureWeek, startDate = futureWeek),
            )

            val initialFavoriteIds = setOf<Long>(0, 1)
            val initialState = ScheduleState(
                list = ViewState.ListState.Content(
                    items = listOf(
                        makeTimeHeader(generatedItems[0]),
                        generatedItems[0].toScheduleListViewData(initialFavoriteIds),
                        generatedItems[1].toScheduleListViewData(initialFavoriteIds),
                        generatedItems[2].toScheduleListViewData(initialFavoriteIds),
                    )
                ),
                timeline = ViewState.TimelineState.Content(
                    items = listOf(
                        generatedItems[0].toTimelineAdapterEventData(initialFavoriteIds, pastMonth, false),
                        generatedItems[1].toTimelineAdapterEventData(initialFavoriteIds, pastMonth, false),
                        generatedItems[2].toTimelineAdapterEventData(initialFavoriteIds, pastMonth, false),
                    )
                ),
            )

            val newFavoriteIds = setOf<Long>(0, 2)
            val expectedState = ScheduleState(
                list = ViewState.ListState.Content(
                    items = listOf(
                        makeTimeHeader(generatedItems[0]),
                        generatedItems[0].toScheduleListViewData(newFavoriteIds),
                        generatedItems[1].toScheduleListViewData(newFavoriteIds),
                        generatedItems[2].toScheduleListViewData(newFavoriteIds),
                    )
                ),
                timeline = ViewState.TimelineState.Content(
                    items = listOf(
                        generatedItems[0].toTimelineAdapterEventData(newFavoriteIds, pastMonth, false),
                        generatedItems[1].toTimelineAdapterEventData(newFavoriteIds, pastMonth, false),
                        generatedItems[2].toTimelineAdapterEventData(newFavoriteIds, pastMonth, false),
                    )
                ),
            )

            val resultState = updateMyScheduleItems(initialState, newFavoriteIds)

            assertThat(resultState).isEqualTo(expectedState)
        }

    }

    @Test
    fun updateSelectedDay_shouldUpdateState() {
        val initialState = ScheduleState(
            header = ViewState.HeaderState(
                datePicker = ViewState.DatePickerState(
                    dates = emptyList(),
                    selectedDate = now,
                    DisplayMode.DAILY
                )
            ),
            selectedView = SelectedView.Timeline,
        )

        val resultState = updateSelectedDay(initialState, futureDay)

        assertThat(resultState).isEqualTo(
            ScheduleState(
                header = ViewState.HeaderState(
                    datePicker = ViewState.DatePickerState(
                        dates = emptyList(),
                        selectedDate = futureDay,
                        DisplayMode.DAILY
                    )
                ),
                selectedView = SelectedView.Timeline,
            )
        )
    }

    @Nested
    @DisplayName("get empty state")
    inner class GetEmptyState {

        fun setupData(
            withMyFavorites: Boolean,
            withMyInterests: Boolean,
        ): ScheduleLayoutData {
            return createScheduleData(
                emptyScheduleImage = "image123",
                timeline = TimelineData(
                    displayToggle = false,
                    defaultDuration = 45,
                    preferredTimeToWidthRatio = 0,
                    buttonIcon = "",
                    emptyStateImage = "image456",
                ),
                myFavorites = if (withMyFavorites) FavoriteConfig(
                    showPicker = false,
                    emptyPage = EmptyPage(
                        "myFavoritesImage",
                        "myFavoritesTitle",
                        "myFavoritesSubtitle",
                        null,
                    )
                ) else null,
                myInterests = if (withMyInterests) IntegratedInterestsData(
                    activeOnLoad = false,
                    emptyPage = EmptyPage(
                        "myInterestsImage",
                        "myInterestsTitle",
                        "myInterestsSubtitle",
                        WidgetCollectionConfiguration.Instance(
                            widgets = listOf(
                                WidgetCollectionConfiguration.Instance.WidgetInfo(
                                    WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                    JsonArray(emptyList()),
                                )
                            )
                        )
                    )
                ) else null
            )
        }

        @Test
        fun `empty state for list full schedule`() {
            val data = setupData(withMyFavorites = false, withMyInterests = false)
            assertThat(
                getEmptyState(
                    selectedSchedule = emptySet(),
                    selectedView = SelectedView.List,
                    scheduleData = data,
                    hasInterests = false
                )
            ).usingRecursiveComparison().isEqualTo(
                ViewState.ListState.Empty(
                    "event.schedule.fullschedule.empty.title",
                    "event.schedule.fullschedule.empty.subtitle",
                    "image123",
                    null,
                    "screenName"
                )
            )
        }

        @Test
        fun `empty state for list - MySchedule`() {
            val data = setupData(withMyFavorites = true, withMyInterests = false)
            assertThat(
                getEmptyState(
                    selectedSchedule = setOf(SelectedSchedule.MySchedule),
                    selectedView = SelectedView.List,
                    scheduleData = data,
                    hasInterests = false
                )
            ).usingRecursiveComparison().isEqualTo(
                ViewState.ListState.Empty(
                    "myFavoritesTitle",
                    "myFavoritesSubtitle",
                    "myFavoritesImage",
                    null,
                    "screenName"
                )
            )
        }

        @Test
        fun `empty state for list - MyInterests`() {
            val data = setupData(withMyFavorites = false, withMyInterests = true)
            assertThat(
                getEmptyState(
                    selectedSchedule = setOf(SelectedSchedule.MyInterests),
                    selectedView = SelectedView.List,
                    scheduleData = data,
                    hasInterests = false
                )
            ).usingRecursiveComparison().isEqualTo(
                ViewState.ListState.Empty(
                    "myInterestsTitle",
                    "myInterestsSubtitle",
                    "myInterestsImage",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                )
            )
        }

        @Test
        fun `empty state for list - MySchedule MyInterests noInterests`() {
            val data = setupData(withMyFavorites = true, withMyInterests = true)
            assertThat(
                getEmptyState(
                    selectedSchedule = setOf(SelectedSchedule.MySchedule, SelectedSchedule.MyInterests),
                    selectedView = SelectedView.List,
                    scheduleData = data,
                    hasInterests = false
                )
            ).usingRecursiveComparison().isEqualTo(
                ViewState.ListState.Empty(
                    "myInterestsTitle",
                    "myInterestsSubtitle",
                    "myInterestsImage",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                )
            )
        }

        @Test
        fun `empty state for list - MySchedule MyInterests hasInterests`() {
            val data = setupData(withMyFavorites = true, withMyInterests = true)
            assertThat(
                getEmptyState(
                    selectedSchedule = setOf(SelectedSchedule.MySchedule, SelectedSchedule.MyInterests),
                    selectedView = SelectedView.List,
                    scheduleData = data,
                    hasInterests = true
                )
            ).usingRecursiveComparison().isEqualTo(
                ViewState.ListState.Empty(
                    "myFavoritesTitle",
                    "myFavoritesSubtitle",
                    "myFavoritesImage",
                    null,
                    "screenName"
                )
            )
        }

        @Test
        fun `empty state for timeline full schedule`() {
            val data = setupData(withMyFavorites = false, withMyInterests = false)
            assertThat(
                getEmptyState(
                    selectedSchedule = emptySet(),
                    selectedView = SelectedView.Timeline,
                    scheduleData = data,
                    hasInterests = false
                )
            ).usingRecursiveComparison().isEqualTo(
                ViewState.ListState.Empty(
                    "event.schedule.timeline.empty.title",
                    "event.schedule.timeline.empty.subtitle",
                    "image456",
                    null,
                    "screenName"
                )
            )
        }

        @Test
        fun `empty state for timeline - MySchedule`() {
            val data = setupData(withMyFavorites = true, withMyInterests = false)
            assertThat(
                getEmptyState(
                    selectedSchedule = setOf(SelectedSchedule.MySchedule),
                    selectedView = SelectedView.Timeline,
                    scheduleData = data,
                    hasInterests = false
                )
            ).usingRecursiveComparison().isEqualTo(
                ViewState.ListState.Empty(
                    "myFavoritesTitle",
                    "myFavoritesSubtitle",
                    "myFavoritesImage",
                    null,
                    "screenName"
                )
            )
        }

        @Test
        fun `empty state for timeline - MyInterests`() {
            val data = setupData(withMyFavorites = false, withMyInterests = true)
            assertThat(
                getEmptyState(
                    selectedSchedule = setOf(SelectedSchedule.MyInterests),
                    selectedView = SelectedView.Timeline,
                    scheduleData = data,
                    hasInterests = false
                )
            ).usingRecursiveComparison().isEqualTo(
                ViewState.ListState.Empty(
                    "myInterestsTitle",
                    "myInterestsSubtitle",
                    "myInterestsImage",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                )
            )
        }

        @Test
        fun `empty state for timeline - MySchedule MyInterests noInterests`() {
            val data = setupData(withMyFavorites = true, withMyInterests = true)
            assertThat(
                getEmptyState(
                    selectedSchedule = setOf(SelectedSchedule.MySchedule, SelectedSchedule.MyInterests),
                    selectedView = SelectedView.Timeline,
                    scheduleData = data,
                    hasInterests = false
                )
            ).usingRecursiveComparison().isEqualTo(
                ViewState.ListState.Empty(
                    "myInterestsTitle",
                    "myInterestsSubtitle",
                    "myInterestsImage",
                    WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    "screenName"
                )
            )
        }

        @Test
        fun `empty state for timeline - MySchedule MyInterests hasInterests`() {
            val data = setupData(withMyFavorites = true, withMyInterests = true)
            assertThat(
                getEmptyState(
                    selectedSchedule = setOf(SelectedSchedule.MySchedule, SelectedSchedule.MyInterests),
                    selectedView = SelectedView.Timeline,
                    scheduleData = data,
                    hasInterests = true
                )
            ).usingRecursiveComparison().isEqualTo(
                ViewState.ListState.Empty(
                    "myFavoritesTitle",
                    "myFavoritesSubtitle",
                    "myFavoritesImage",
                    null,
                    "screenName"
                )
            )
        }
    }

    private fun TimedScheduleItem.toScheduleListViewData(favoriteIds: Set<Long>) =
        ScheduleListViewData.ScheduleItem(
            itemId = scheduleItem.itemId,
            name = scheduleItem.name,
            timeSlot = timeSlot,
            stageId = stage?.id,
            stageLabel = stage?.name,
            stageOrder = stage?.order,
            photo = scheduleItem.photos.takeIf { it.isNotEmpty() }?.get(0),
            isInMySchedule = favoriteIds.contains(scheduleItem.itemId),
            hideEndTime = false
        )

    private fun makeTimeHeader(timedScheduleItem: TimedScheduleItem) =
        ScheduleListViewData.HeaderItem.TimeHeaderItem(
            startTime = timedScheduleItem.timeSlot.startDate!!.getFormattedDateTime(
                null,
                FormatStyle.SHORT,
                timezoneProvider.zoneId,
            )
        )

    private fun makeDayHeader(timedScheduleItem: TimedScheduleItem) = ScheduleListViewData.HeaderItem.DayHeaderItem(
        day = timedScheduleItem.timeSlot.dayOfEvent.getFormattedDateTime(
            FormatStyle.FULL,
            null,
            timezoneProvider.zoneId
        )
    )

    private fun makeNextButton(label: String, nextDate: ZonedDateTime) =
        ScheduleListViewData.NextDateButton(
            label = label,
            nextDate = nextDate
        )

    private fun TimedScheduleItem.toTimelineAdapterEventData(
        favoriteIds: Set<Long>,
        computedEndDate: ZonedDateTime,
        hideEndTime: Boolean,
    ) = TimelineAdapter.EventData(
        id = scheduleItem.itemId,
        name = scheduleItem.name,
        stageId = stage?.id,
        stageLabel = stage?.name,
        stageOrder = stage?.order,
        isInMySchedule = favoriteIds.contains(scheduleItem.itemId),
        startDate = timeSlot.startDate!!,
        endDate = timeSlot.endDate.takeUnless { hideEndTime },
        computedEndDate = computedEndDate,
    )
}
