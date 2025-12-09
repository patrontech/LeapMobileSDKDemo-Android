package com.greencopper.event.scheduleItem.viewmodel

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.scheduleItem.ScheduleLayoutData
import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.filtering.*
import com.greencopper.interfacekit.filtering.FilteringHandler.Mode
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.filtering.filteringbar.FilteringButton
import com.greencopper.interfacekit.interests.integration.IntegratedInterestsData
import com.greencopper.interfacekit.topbar.TopBarState
import com.greencopper.interfacekit.ui.views.navigationcontrols.KibaToolbar
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.*
import com.greencopper.testmocks.interfacekit.MockFilteringHandler
import com.greencopper.toolkit.Toolkit
import io.mockk.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ScheduleListViewModelTest : CoroutineTest(UnconfinedTestDispatcher()) {

    private lateinit var viewModel: ScheduleListViewModel

    private val localizationService = MockLocalizationService()
    private val filteringHandler = MockFilteringHandler()
    private val mockZoneId = ZoneId.of("America/Phoenix")
    private val timezoneProvider = MockTimezoneProvider(mockZoneId)
    private val store: MockStore<ScheduleState, ScheduleAction> = MockStore(ScheduleState())
    private val conditionChecker = MockConditionChecker()

    init {
        Toolkit.setupTest()
        bindSingleton<TimezoneProvider>(timezoneProvider)
    }

    override fun afterEach() {
        unmockkAll()
    }

    @Test
    fun getFilteringBarData_shouldReturnMockedList() {
        setupViewModel()
        filteringHandler.mockedFilteringBarData = FilteringBarData(emptyList())
        val filteringBarData = viewModel.getFilteringBarData(DialogFragment())
        assertThat(filteringBarData.filters).isEmpty()
    }

    @Test
    fun getCurrentFilterState_shouldReturnMockedCurrentStateInfo() {
        setupViewModel()
        val result = viewModel.getCurrentFilterState()
        assertThat(result.mode).isEqualTo(Mode.DEFAULT)
        assertThat(viewModel.getCurrentFilterState().filteringInfo).isNull()

        val testFilteringInfo = FilteringInfo(
            predicate = FilteringPredicate.Tag("tag")
        )
        filteringHandler.mockedCurrentStateToInfo = testFilteringInfo
        assertThat(viewModel.getCurrentFilterState().mode).isEqualTo(Mode.DEFAULT)
        assertThat(viewModel.getCurrentFilterState().filteringInfo)
            .usingRecursiveComparison()
            .isEqualTo(testFilteringInfo)
    }

    @Nested
    @DisplayName("FilteringBar buttons")
    inner class FilterBarButtonTests {

        init {
            localizationService.getStringFromRepository = {
                if (it == "event.schedule.timeline.toggle") null
                else it
            }
        }

        @Test
        fun noFavoritesNoTimeline() {
            //given
            setupViewModel()

            //when
            val filteringBarButtons = viewModel.getFilteringBarButtons()

            //then
            assertThat(filteringBarButtons.size).isEqualTo(0)
        }

        @Test
        fun favoritesOnly() {
            //given
            setupViewModel(
                createScheduleData(
                    myFavorites = FavoriteConfig(
                        activeOnLoad = false,
                        filteringButton = FilteringButton(
                            FilteringButton.Button(
                                title = "titleSelected",
                                accessibilityLabel = "titleSelected"
                            ),
                            FilteringButton.Button(
                                title = "titleUnselected",
                                accessibilityLabel = "titleUnselected"
                            )
                        ),
                        emptyPage = EmptyPage("title", "subtitle", "image")
                    )
                )
            )
            store.mutableState.value = ScheduleState(
                selectedSchedule = emptySet()
            )

            //when
            val filteringBarButtons = viewModel.getFilteringBarButtons()
            val favoritesButton = filteringBarButtons.firstOrNull()

            //then
            assertThat(filteringBarButtons.size).isEqualTo(1)
            assertThat(favoritesButton?.default?.title).isEqualTo("titleUnselected")
            assertThat(favoritesButton?.isCheckedAtSetup).isFalse

            favoritesButton?.onButtonToggled?.invoke(true)
            assertThat(store.actionSent.size).isEqualTo(1)
            assertThat(store.actionSent.first()).isEqualTo(ScheduleAction.User.TappedMySchedule(true))

            store.actionSent.clear()
            favoritesButton?.onButtonToggled?.invoke(false)
            assertThat(store.actionSent.size).isEqualTo(1)
            assertThat(store.actionSent.first()).isEqualTo(ScheduleAction.User.TappedMySchedule(false))

            //given
            store.mutableState.value = ScheduleState(
                selectedSchedule = setOf(SelectedSchedule.MySchedule)
            )

            //then
            assertThat(viewModel.getFilteringBarButtons().firstOrNull()?.isCheckedAtSetup).isTrue
        }

        @Test
        fun timelineOnly() {
            //given
            setupViewModel(
                createScheduleData(
                    timeline = TimelineData(
                        displayToggle = true,
                        defaultDuration = 0,
                        preferredTimeToWidthRatio = 0,
                        buttonIcon = "icon",
                        emptyStateImage = "image"
                    )
                )
            )
            store.mutableState.value = ScheduleState(
                selectedView = SelectedView.List
            )

            //when
            val filteringBarButtons = viewModel.getFilteringBarButtons()
            val timelineButton = filteringBarButtons.firstOrNull()

            //then
            assertThat(filteringBarButtons.size).isEqualTo(1)
            assertThat(timelineButton?.default?.title).isEqualTo(null)
            assertThat(timelineButton?.isCheckedAtSetup).isFalse

            timelineButton?.onButtonToggled?.invoke(true)
            assertThat(store.actionSent.size).isEqualTo(1)
            assertThat(store.actionSent.first()).isEqualTo(ScheduleAction.User.ViewSelected(SelectedView.Timeline))

            store.actionSent.clear()
            timelineButton?.onButtonToggled?.invoke(false)
            assertThat(store.actionSent.size).isEqualTo(1)
            assertThat(store.actionSent.first()).isEqualTo(ScheduleAction.User.ViewSelected(SelectedView.List))

            //given
            store.mutableState.value = ScheduleState(
                selectedView = SelectedView.Timeline
            )
            //then
            assertThat(viewModel.getFilteringBarButtons().firstOrNull()?.isCheckedAtSetup).isTrue

            //given
            setupViewModel(
                createScheduleData(
                    timeline = TimelineData(
                        displayToggle = false, //changing default visibility
                        defaultDuration = 0,
                        preferredTimeToWidthRatio = 0,
                        buttonIcon = "icon",
                        emptyStateImage = "image"
                    )
                )
            )
            //then
            assertThat(viewModel.getFilteringBarButtons()).isEmpty()
        }

        @Test
        fun interestsOnly() {
            //given
            setupViewModel(
                createScheduleData(
                    myInterests = IntegratedInterestsData(
                        activeOnLoad = false,
                        filteringButton = FilteringButton(
                            FilteringButton.Button(
                                title = "titleSelected",
                                accessibilityLabel = "titleSelected"
                            ),
                            FilteringButton.Button(
                                title = "titleUnselected",
                                accessibilityLabel = "titleUnselected"
                            )
                        ),
                        emptyPage = EmptyPage(
                            title = "title",
                            subtitle = "subtitle",
                            image = "image"
                        )
                    )
                )
            )
            store.mutableState.value = ScheduleState(
                selectedSchedule = emptySet()
            )

            //when
            val filteringBarButtons = viewModel.getFilteringBarButtons()
            val interestsButton = filteringBarButtons.firstOrNull()

            //then
            assertThat(filteringBarButtons.size).isEqualTo(1)
            assertThat(interestsButton?.default?.title).isEqualTo("titleUnselected")
            assertThat(interestsButton?.isCheckedAtSetup).isFalse

            interestsButton?.onButtonToggled?.invoke(true)
            assertThat(store.actionSent.size).isEqualTo(1)
            assertThat(store.actionSent.first()).isEqualTo(ScheduleAction.User.TappedMyInterests(true))

            store.actionSent.clear()
            interestsButton?.onButtonToggled?.invoke(false)
            assertThat(store.actionSent.size).isEqualTo(1)
            assertThat(store.actionSent.first()).isEqualTo(ScheduleAction.User.TappedMyInterests(false))

            //given
            store.mutableState.value = ScheduleState(
                selectedSchedule = setOf(SelectedSchedule.MyInterests)
            )

            //then
            assertThat(viewModel.getFilteringBarButtons().firstOrNull()?.isCheckedAtSetup).isTrue
        }

        @Test
        fun allButtons() {
            //given
            setupViewModel(
                createScheduleData(
                    myFavorites = FavoriteConfig(
                        activeOnLoad = false,
                        filteringButton = FilteringButton(
                            FilteringButton.Button(
                                title = null,
                                icon = "iconSelected3",
                                accessibilityLabel = "titleSelected4"
                            ),
                            FilteringButton.Button(
                                title = null,
                                icon = "iconUnselected3",
                                accessibilityLabel = "titleUnselected4"
                            )
                        ),
                        emptyPage = EmptyPage("title", "subtitle", "image")
                    ),
                    timeline = TimelineData(
                        displayToggle = true,
                        defaultDuration = 0,
                        preferredTimeToWidthRatio = 0,
                        buttonIcon = "icon",
                        emptyStateImage = "image"
                    ),
                    myInterests = IntegratedInterestsData(
                        activeOnLoad = false,
                        filteringButton = FilteringButton(
                            FilteringButton.Button(
                                title = "titleSelected1",
                                accessibilityLabel = "titleSelected2"
                            ),
                            FilteringButton.Button(
                                title = "titleUnselected1",
                                accessibilityLabel = "titleUnselected2"
                            )
                        ),
                        emptyPage = EmptyPage(
                            title = "title",
                            subtitle = "subtitle",
                            image = "image"
                        )
                    )
                )
            )

            //when
            val filteringBarButtons = viewModel.getFilteringBarButtons().reversed()
            val timelineButton = filteringBarButtons.getOrNull(0)
            val interestsButton = filteringBarButtons.getOrNull(1)
            val favoritesButton = filteringBarButtons.getOrNull(2)

            //then
            assertThat(filteringBarButtons.size).isEqualTo(3)
            assertThat(favoritesButton?.default?.title).isEqualTo(null)
            assertThat(favoritesButton?.default?.icon).isEqualTo("iconUnselected3")
            assertThat(timelineButton?.default?.title).isEqualTo(null)
            assertThat(interestsButton?.default?.title).isEqualTo("titleUnselected1")
        }
    }

    @Test
    fun initialSetup_shouldSendAndReturnOnceDone() {
        val topBar = TopBarState<ScheduleAction>(
            listOf(
                TopBarState.TopBarButton(
                    title = null,
                    icon = null,
                    iconResource = null,
                    side = KibaToolbar.Side.RIGHT,
                    shouldColor = false,
                    accessibilityLabel = null,
                    id = null,
                    onClick = ScheduleAction.User.TappedSearch
                )
            )
        )
        store.mutableState.value = ScheduleState(header = ViewState.HeaderState(topBar = topBar))
        setupViewModel()

        var topBarState: TopBarState<ScheduleAction>? = null
        runTest {
            topBarState = viewModel.getInitialSetup(layout = mockk())
        }


        assertThat(store.actionSent.first()).isInstanceOf(ScheduleAction.ScreenLoaded::class.java)
        assertThat(topBarState).isEqualTo(topBar)
    }

    @Test
    fun getDatePickerState_shouldReturnFlow() {
        //given
        setupViewModel()
        val datePicker = ViewState.DatePickerState(emptyList(), ZonedDateTime.now(), DisplayMode.DAILY)
        store.mutableState.value = ScheduleState(
            header = ViewState.HeaderState(
                datePicker = datePicker
            )
        )
        testScope.launch {
            viewModel.datePickerState.collect()
        }

        //when
        val datePickerState = viewModel.datePickerState.value

        //then
        assertThat(datePickerState).isEqualTo(datePicker)
    }

    @Nested
    @DisplayName("List State")
    inner class List_State {

        @Test
        fun getListState_withContent_andCorrectSelectedView_shouldReturnFlow() {
            //given
            setupViewModel()
            val list = ViewState.ListState.Content(emptyList())
            store.mutableState.value = ScheduleState(
                list = list,
                selectedView = SelectedView.List,
            )
            testScope.launch {
                viewModel.listState.collect()
            }

            //when
            val listState = viewModel.listState.value

            //then
            assertThat(listState).isEqualTo(list)
        }

        @Test
        fun getListState_withEmpty_andCorrectSelectedView_shouldReturnNullFlow() {
            //given
            setupViewModel()
            store.mutableState.value = ScheduleState(
                list = ViewState.ListState.Empty("title", "subtitle", "imageName", null, "screenName"),
                selectedView = SelectedView.List,
            )
            testScope.launch {
                viewModel.listState.collect()
            }

            //when
            val listState = viewModel.listState.value

            //then
            assertThat(listState).isNull()
        }

        @Test
        fun getListState_withContent_andCorrectSelectedView_shouldReturnNullFlow() {
            //given
            setupViewModel()
            val list = ViewState.ListState.Content(emptyList())
            store.mutableState.value = ScheduleState(
                list = list,
                selectedView = SelectedView.Timeline,
            )
            testScope.launch {
                viewModel.listState.collect()
            }

            //when
            val listState = viewModel.listState.value

            //then
            assertThat(listState).isNull()
        }

        @Test
        fun findNextScheduledItemPosition_shouldReturnCorrectPosition() {
            // given
            setupViewModel()
            val now = ZonedDateTime.now()
            val items = listOf(
                mockk<ScheduleListViewData.HeaderItem.TimeHeaderItem>(relaxed = true),
                ScheduleListViewData.ScheduleItem(
                    itemId = 1L,
                    name = "Event 1",
                    timeSlot = mockk<TimeSlot>(relaxed = true) {
                        every { startDate } returns now.minusHours(2)
                        every { endDate } returns now.minusHours(1)
                    },
                    isInMySchedule = false,
                    hideEndTime = false,
                    stageId = null,
                    stageLabel = null,
                    stageOrder = null,
                    photo = null,
                ),
                mockk<ScheduleListViewData.HeaderItem.TimeHeaderItem>(relaxed = true),
                ScheduleListViewData.ScheduleItem(
                    itemId = 2L,
                    name = "Event 2",
                    timeSlot = mockk<TimeSlot>(relaxed = true) {
                        every { startDate } returns now.plusMinutes(30)
                        every { endDate } returns now.plusMinutes(60)
                    },
                    isInMySchedule = false,
                    hideEndTime = false,
                    stageId = null,
                    stageLabel = null,
                    stageOrder = null,
                    photo = null,
                ),
                mockk<ScheduleListViewData.HeaderItem.TimeHeaderItem>(relaxed = true),
                ScheduleListViewData.ScheduleItem(
                    itemId = 3L,
                    name = "Event 3",
                    timeSlot = mockk<TimeSlot>(relaxed = true) {
                        every { startDate } returns now.plusHours(1)
                        every { endDate } returns now.plusHours(2)
                    },
                    isInMySchedule = false,
                    hideEndTime = false,
                    stageId = null,
                    stageLabel = null,
                    stageOrder = null,
                    photo = null,
                )
            )

            // when
            val position = viewModel.findNextScheduledItemPosition(items)

            // then
            assertThat(position).isEqualTo(2)
        }

        @Test
        fun findNextScheduledItemPosition_shouldReturnZeroWhenNoFutureEvents() {
            // given
            setupViewModel()
            val now = ZonedDateTime.now(timezoneProvider.zoneId)
            val items = listOf(
                ScheduleListViewData.ScheduleItem(
                    itemId = 1L,
                    name = "Event 1",
                    timeSlot = mockk<TimeSlot>(relaxed = true) {
                        every { startDate } returns now.minusHours(2)
                        every { endDate } returns now.minusHours(1)
                    },
                    isInMySchedule = false,
                    hideEndTime = false,
                    stageId = null,
                    stageLabel = null,
                    stageOrder = null,
                    photo = null,
                ),
                ScheduleListViewData.ScheduleItem(
                    itemId = 2L,
                    name = "Event 2",
                    timeSlot = mockk<TimeSlot>(relaxed = true) {
                        every { startDate } returns now.minusMinutes(30)
                        every { endDate } returns now.minusMinutes(10)
                    },
                    isInMySchedule = false,
                    hideEndTime = false,
                    stageId = null,
                    stageLabel = null,
                    stageOrder = null,
                    photo = null,
                )
            )

            // when
            val position = viewModel.findNextScheduledItemPosition(items)

            // then
            assertThat(position).isEqualTo(0)
        }

        @Test
        fun findNextScheduledItemPosition_shouldReturnZeroWhenListIsEmpty() {
            // given
            setupViewModel()
            val items = emptyList<ScheduleListViewData>()

            // when
            val position = viewModel.findNextScheduledItemPosition(items)

            // then
            assertThat(position).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("Timeline State")
    inner class Timeline_State {

        @Test
        fun getTimelineState_withContent_andCorrectSelectedView_shouldReturnFlow() {
            //given
            setupViewModel()
            val timeline = ViewState.TimelineState.Content(emptyList())
            store.mutableState.value = ScheduleState(
                timeline = timeline,
                selectedView = SelectedView.Timeline,
            )
            testScope.launch {
                viewModel.timelineState.collect()
            }

            //when
            val timelineState = viewModel.timelineState.value

            //then
            assertThat(timelineState).isEqualTo(timeline)
        }

        @Test
        fun getTimelineState_withEmpty_andCorrectSelectedView_shouldReturnNullFlow() {
            //given
            setupViewModel()
            store.mutableState.value = ScheduleState(
                timeline = ViewState.TimelineState.Empty("title", "subtitle", "imageName", null, "screenName"),
                selectedView = SelectedView.Timeline,
            )
            testScope.launch {
                viewModel.timelineState.collect()
            }

            //when
            val timelineState = viewModel.timelineState.value

            //then
            assertThat(timelineState).isNull()
        }

        @Test
        fun getTimelineState_withContent_andCorrectSelectedView_shouldReturnNullFlow() {
            //given
            setupViewModel()
            val timeline = ViewState.TimelineState.Content(emptyList())
            store.mutableState.value = ScheduleState(
                timeline = timeline,
                selectedView = SelectedView.List,
            )
            testScope.launch {
                viewModel.timelineState.collect()
            }

            //when
            val timelineState = viewModel.timelineState.value

            //then
            assertThat(timelineState).isNull()
        }
    }

    @Test
    fun onDatePickerDateTap_shouldSendAction() {
        //given
        setupViewModel()
        val selectedDate = ZonedDateTime.now()

        //when
        viewModel.onDatePickerDateTap(selectedDate)

        //then
        assertThat(store.actionSent.size).isEqualTo(1)
        assertThat(store.actionSent.firstOrNull()).isEqualTo(ScheduleAction.User.TappedDay(selectedDate))
    }

    @Test
    fun onScheduleItemTap_shouldSendAction() {
        //given
        setupViewModel()
        val itemId = 123L

        //when
        viewModel.onScheduleItemTap(itemId)

        //then
        assertThat(store.actionSent.size).isEqualTo(1)
        assertThat(store.actionSent.firstOrNull()).isEqualTo(ScheduleAction.User.TappedScheduleItem(itemId))
    }

    @Test
    fun onNextDateTap_shouldSendAction() {
        //given
        setupViewModel()
        val nextDate = ZonedDateTime.now()

        //when
        viewModel.onNextDateTap(nextDate)

        //then
        assertThat(store.actionSent.size).isEqualTo(1)
        assertThat(store.actionSent.firstOrNull()).isEqualTo(ScheduleAction.User.TappedNext(nextDate))
    }

    @Test
    fun sendAction_shouldSendAction() {
        //given
        setupViewModel()
        val action = ScheduleAction.User.TappedScheduleItem(123L)

        //when
        viewModel.sendAction(action)

        //then
        assertThat(store.actionSent.size).isEqualTo(1)
        assertThat(store.actionSent.firstOrNull()).isEqualTo(action)
    }

    @Test
    fun onAddRemoveFavoritesTap_shouldSendCorrectAction() {
        //given
        setupViewModel()
        val itemId = 123L
        val name = "name"
        val startDate = ZonedDateTime.now()

        //when
        viewModel.onAddRemoveFavoritesTap(
            isInMySchedule = false,
            itemId,
            name,
            startDate
        )

        //then
        assertThat(store.actionSent.size).isEqualTo(1)
        assertThat(store.actionSent.firstOrNull()).isEqualTo(
            ScheduleAction.User.TappedAddToMySchedule(
                itemId,
                name,
                startDate
            )
        )

        //when
        store.actionSent.clear()
        viewModel.onAddRemoveFavoritesTap(
            isInMySchedule = true,
            itemId,
            name,
            null
        )

        //then
        assertThat(store.actionSent.size).isEqualTo(1)
        assertThat(store.actionSent.firstOrNull()).isEqualTo(
            ScheduleAction.User.TappedRemoveFromMySchedule(
                itemId,
                name,
                null
            )
        )
    }

    @Test
    fun saveState_shouldAddStateToBundle() {
        //given
        setupViewModel()

        val scheduleState = ScheduleState(
            header = ViewState.HeaderState(
                datePicker = ViewState.DatePickerState(emptyList(), ZonedDateTime.now(), DisplayMode.DAILY)
            )
        )
        store.mutableState.value = scheduleState

        val addedToBundle = slot<String>()

        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), capture(addedToBundle)) } returns Unit

        val bundle = Bundle()

        //when
        viewModel.saveState(bundle, "123")

        //then
        assertThat(addedToBundle.captured).isEqualTo(scheduleState.encodeToString())
    }

    @Test
    fun updatingFilter_shouldSendAction() {
        //given
        setupViewModel()
        var updateCounter = 0

        testScope.launch {
            viewModel.filteringUpdater.collectLatest { updateCounter++ }
        }

        //when
        filteringHandler.mockedPredicate = MockFilteringPredicateComputed("query", true)

        //then
        assertThat(updateCounter).isEqualTo(1)
        assertThat(store.actionSent.size).isEqualTo(1)
        assertThat(store.actionSent.first()).isEqualTo(ScheduleAction.LoadItems)
    }

    private fun setupViewModel(
        scheduleData: ScheduleLayoutData = createScheduleData(),
    ) {
        viewModel = ScheduleListViewModel(
            localizationService,
            filteringHandler,
            timezoneProvider,
            scheduleData,
            store,
            testScope,
            conditionChecker,
        )
    }

}
