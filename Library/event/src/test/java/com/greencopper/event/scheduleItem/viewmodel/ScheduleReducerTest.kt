package com.greencopper.event.scheduleItem.viewmodel

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.R
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.recipe.EventConfiguration
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.event.scheduleItem.ScheduleData
import com.greencopper.eventmocks.*
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.favorites.toFavoriteable
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.interests.integration.IntegratedInterestsData
import com.greencopper.interfacekit.interests.recipe.*
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.topbar.TopBarState
import com.greencopper.interfacekit.ui.views.navigationcontrols.KibaToolbar
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.*
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.test.testReduce
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ScheduleReducerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    private lateinit var reducer: ScheduleReducer
    private var initialState = ScheduleState()

    private val timedScheduleItemRepository = MockTimedScheduleItemRepository()
    private val localizationService = MockLocalizationService()
    private val filteringHandler = MockFilteringHandler()
    private val timezoneProvider = MockTimezoneProvider(ZoneId.of("America/Phoenix"))
    private val reminderUIManager = MockReminderUIManager()
    private var myScheduleManager = MockMyScheduleManager(emptySet(), reminderUIManager)
    private val eventConfigHolder = EventConfigurationHolder()
    private val interestsConfigHolder = InterestsConfigurationHolder()
    private val widgetResolver = MockWidgetResolver()
    private val routeController = MockRouteController()
    private var scheduleData = createScheduleData()
    private val conditionChecker = MockConditionChecker()
    private val localStorage = LocalStorage("test")

    init {
        Toolkit.setupTest()
        bindSingleton<TimezoneProvider>(timezoneProvider)
    }

    override fun afterEach() {
        unmockkAll()
    }

    @Nested
    @DisplayName("ScreenLoaded Action")
    inner class ScreenLoaded {

        @Test
        fun whenButtons_activeOnLoad_shouldSelectCorrectSchedule() = runTest {
            scheduleData = createScheduleData(
                myFavorites = FavoriteConfig(activeOnLoad = true, emptyPage = EmptyPage("title", "subtitle", "image")),
                myInterests = IntegratedInterestsData(
                    activeOnLoad = true,
                    emptyPage = EmptyPage(title = "title", subtitle = "subtitle", image = "image")
                ),
            )
            testFunction(
                selectedSchedule = setOf(SelectedSchedule.MySchedule, SelectedSchedule.MyInterests),
                topBarButtons = listOf()
            )
        }

        @Test
        fun whenNoFavorites_shouldSelectCorrectSchedule() = runTest {
            scheduleData = createScheduleData()
            testFunction(
                selectedSchedule = emptySet(),
                topBarButtons = listOf()
            )
        }

        @Test
        fun whenRemindersAvailable_shouldHaveRemindersButton() = runTest {
            scheduleData = createScheduleData()
            eventConfigHolder.currentConfiguration.value = EventConfiguration(
                reminders = EventConfiguration.Reminders(
                    topBarIcon = "topBarIcon",
                    timeIntervals = listOf(),
                    defaultTimeInterval = 0,
                    onFirstAddToMyScheduleRouteLink = "",
                    onNotificationTapRouteLink = ""
                )
            )
            testFunction(
                selectedSchedule = emptySet(),
                topBarButtons = listOf(
                    TopBarState.TopBarButton(
                        title = null,
                        icon = "topBarIcon",
                        iconResource = null,
                        side = KibaToolbar.Side.RIGHT,
                        accessibilityLabel = "event.schedule.reminders_icon.accessibility_label",
                        onClick = ScheduleAction.User.TappedScheduleReminders
                    )
                )
            )
        }

        @Test
        fun whenSearchAvailable_shouldHaveSearchButton() = runTest {
            scheduleData = createScheduleData(search = Search("searchRouteLink"))
            testFunction(
                selectedSchedule = emptySet(),
                topBarButtons = listOf(
                    TopBarState.TopBarButton(
                        title = null,
                        icon = null,
                        iconResource = R.drawable.ic_search,
                        side = KibaToolbar.Side.RIGHT,
                        accessibilityLabel = "common.search",
                        onClick = ScheduleAction.User.TappedSearch
                    )
                )
            )
        }

        @Test
        fun whenEditInterestsAvailable_shouldHaveEditInterestsButton() = runTest {
            scheduleData = createScheduleData(editMyInterests = ScheduleData.EditMyInterests("xyz://editMyInterests"))
            testFunction(
                selectedSchedule = emptySet(),
                topBarButtons = listOf(
                    TopBarState.TopBarButton(
                        title = null,
                        icon = null,
                        iconResource = R.drawable.ic_interests_edit,
                        side = KibaToolbar.Side.RIGHT,
                        accessibilityLabel = "event.schedule.edit_my_interests.accessibility_label",
                        onClick = ScheduleAction.User.TappedEditMyInterests
                    )
                )
            )
        }

        @Test
        fun whenRemindersAndSearchAvailable_shouldHaveBothButtons_inCorrectOrder() = runTest {
            scheduleData = createScheduleData(search = Search("searchRouteLink"))
            eventConfigHolder.currentConfiguration.value = EventConfiguration(
                reminders = EventConfiguration.Reminders(
                    topBarIcon = "topBarIcon",
                    timeIntervals = listOf(),
                    defaultTimeInterval = 0,
                    onFirstAddToMyScheduleRouteLink = "",
                    onNotificationTapRouteLink = ""
                )
            )
            testFunction(
                selectedSchedule = emptySet(),
                topBarButtons = listOf(
                    TopBarState.TopBarButton(
                        title = null,
                        icon = "topBarIcon",
                        iconResource = null,
                        side = KibaToolbar.Side.RIGHT,
                        accessibilityLabel = "event.schedule.reminders_icon.accessibility_label",
                        onClick = ScheduleAction.User.TappedScheduleReminders
                    ),
                    TopBarState.TopBarButton(
                        title = null,
                        icon = null,
                        iconResource = R.drawable.ic_search,
                        side = KibaToolbar.Side.RIGHT,
                        accessibilityLabel = "common.search",
                        onClick = ScheduleAction.User.TappedSearch
                    )
                )
            )
        }

        private fun testFunction(
            selectedSchedule: Set<SelectedSchedule>,
            topBarButtons: List<TopBarState.TopBarButton<ScheduleAction>>,
        ) = runTest {
            createReducer()
            val layout = mockk<Layout>()
            reducer.testReduce(initialState, ScheduleAction.ScreenLoaded(layout)) { state, effect ->
                assertThat(state).isEqualTo(
                    initialState.copy(
                        layout = layout,
                        selectedSchedule = selectedSchedule,
                        selectedView = scheduleData.defaultUI,
                        header = ViewState.HeaderState(
                            topBar = TopBarState(
                                buttons = topBarButtons
                            )
                        )
                    )
                )
                effect shouldBeActions arrayOf(
                    ScheduleAction.LoadItems,
                    ScheduleAction.MyScheduleIdsUpdated(emptySet()),
                    ScheduleAction.InterestsUpdated(emptySet()),
                )
            }
        }
    }

    @Nested
    @DisplayName("TappedMySchedule Action")
    inner class TappedMySchedule {
        init {
            initialState = initialState.copy(
                selectedSchedule = setOf(SelectedSchedule.MyInterests)
            )
        }

        @Test
        fun selectingFullSchedule_shouldUpdateState_shouldUpdateFilteringHandler() {
            filteringHandler.currentModeValue = FilteringHandler.Mode.MY_FAVORITES
            testFunction(false)
        }

        @Test
        fun selectingMySchedule_shouldUpdateState_shouldUpdateFilteringHandler() {
            filteringHandler.currentModeValue = FilteringHandler.Mode.DEFAULT
            testFunction(true)
        }

        private fun testFunction(
            selected: Boolean,
        ) = runTest {
            createReducer()
            reducer.testReduce(initialState, ScheduleAction.User.TappedMySchedule(selected)) { state, effect ->
                val expectedSelectedSchedule = if (selected) {
                    setOf(SelectedSchedule.MySchedule, SelectedSchedule.MyInterests)
                } else {
                    setOf(SelectedSchedule.MyInterests)
                }

                assertThat(state).isEqualTo(
                    initialState.copy(
                        selectedSchedule = expectedSelectedSchedule,
                    )
                )
                assertThat(effect).isEqualTo(NoEffect)

                assertThat(filteringHandler.currentModeValue).isEqualTo(
                    if (selected) {
                        FilteringHandler.Mode.MY_FAVORITES
                    } else {
                        FilteringHandler.Mode.DEFAULT
                    }
                )
            }
        }
    }

    @Nested
    @DisplayName("TappedMyInterests Action")
    inner class TappedMyInterests {
        init {
            initialState = initialState.copy(
                selectedSchedule = setOf(SelectedSchedule.MySchedule)
            )
        }

        @Test
        fun disablingMyInterests_shouldUpdateState() {
            testFunction(false)
        }

        @Test
        fun enablingMyInterests_shouldUpdateState() {
            testFunction(true)
        }

        private fun testFunction(
            selected: Boolean,
        ) = runTest {
            createReducer()
            reducer.testReduce(initialState, ScheduleAction.User.TappedMyInterests(selected)) { state, effect ->
                val expectedSelectedSchedule = if (selected) {
                    setOf(SelectedSchedule.MySchedule, SelectedSchedule.MyInterests)
                } else {
                    setOf(SelectedSchedule.MySchedule)
                }

                assertThat(state).isEqualTo(
                    initialState.copy(
                        selectedSchedule = expectedSelectedSchedule,
                    )
                )

                effect shouldBeAction ScheduleAction.LoadItems
            }
        }
    }

    @Nested
    @DisplayName("TappedAddToMySchedule Action")
    inner class TappedAddToMySchedule {

        @Test
        fun tappedAddToMySchedule_shouldShowPopup_shouldAddToFavorites() = runTest {
            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.User.TappedAddToMySchedule(
                    1L,
                    "itemName",
                    ZonedDateTime.now(),
                )
            ) { state, effect ->
                assertThat(state).isEqualTo(initialState)
                assertThat(effect).isEqualTo(NoEffect)

                assertThat(reminderUIManager.onAddToMyScheduleCalled).isTrue
                assertThat(myScheduleManager.favoriteIds).isEqualTo(setOf(1L))
            }
        }
    }

    @Nested
    @DisplayName("TappedRemoveFromMySchedule Action")
    inner class TappedRemoveFromMySchedule {

        @Test
        fun tappedRemoveFromMySchedule_shouldRemoveFromFavorites() = runTest {
            myScheduleManager.addToFavorites(1L.toFavoriteable())
            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.User.TappedRemoveFromMySchedule(
                    1L,
                    "itemName",
                    ZonedDateTime.now(),
                )
            ) { state, effect ->
                assertThat(state).isEqualTo(initialState)
                assertThat(effect).isEqualTo(NoEffect)

                assertThat(myScheduleManager.favoriteIds).isEqualTo(emptySet<Long>())
            }
        }
    }

    @Nested
    @DisplayName("TappedDay Action")
    inner class TappedDay {

        @Test
        fun tappedDay_shouldUpdateDate_triggerLoadContent() = runTest {
            val expectedState = initialState.copy()
            val date = ZonedDateTime.now()
            mockkStatic(::updateSelectedDay)
            every { updateSelectedDay(any(), date) } returns expectedState

            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.User.TappedDay(date)
            ) { state, effect ->
                assertThat(state).isSameAs(expectedState)
                effect shouldBeAction ScheduleAction.LoadContent(purge = true)
            }
        }
    }

    @Nested
    @DisplayName("TappedNext Action")
    inner class TappedNext {

        @Test
        fun tappedNext_shouldUpdateDate_triggerLoadContent() = runTest {
            val expectedState = initialState.copy()
            val date = ZonedDateTime.now()
            mockkStatic(::updateSelectedDay)
            every { updateSelectedDay(any(), date) } returns expectedState

            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.User.TappedNext(date)
            ) { state, effect ->
                assertThat(state).isSameAs(expectedState)
                effect shouldBeAction ScheduleAction.LoadContent(purge = true)
            }
        }
    }

    @Nested
    @DisplayName("TappedSearch Action")
    inner class TappedSearch {

        private val searchRouteLink = "searchRouteLink"

        private fun testFunction(shouldSucceed: Boolean) = runTest {
            createReducer()
            reducer.testReduce(initialState, ScheduleAction.User.TappedSearch) { state, effect ->
                assertThat(state).isEqualTo(initialState)
                assertThat(effect).isEqualTo(NoEffect)
            }

            assertThat(routeController.lastResolveRouteLink != null).isEqualTo(shouldSucceed)
            assertThat(routeController.lastResolveRouteLink == searchRouteLink).isEqualTo(shouldSucceed)
        }

        @Test
        fun withoutLayout_shouldFail() {
            testFunction(shouldSucceed = false)
        }

        @Test
        fun withoutSearch_shouldFail() {
            initialState = initialState.copy(layout = mockk())
            testFunction(shouldSucceed = false)
        }

        @Test
        fun withLayoutAndSearch_shouldSucceed() {
            initialState = initialState.copy(layout = mockk())
            scheduleData = createScheduleData(
                search = Search(
                    onTapRouteLink = searchRouteLink
                )
            )
            testFunction(shouldSucceed = true)
        }
    }

    @Nested
    @DisplayName("TappedScheduleItem Action")
    inner class TappedScheduleItem {

        @Test
        fun withoutLayout_shouldDoNothing() = runTest {
            testFunction(expectRouting = false)
        }

        @Test
        fun withLayout_shouldRouteToLink() = runTest {
            initialState = initialState.copy(layout = mockk())
            testFunction(expectRouting = true)
        }

        private fun testFunction(expectRouting: Boolean) = runTest {
            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.User.TappedScheduleItem(1L)
            ) { state, effect ->
                assertThat(state).isEqualTo(initialState)
                assertThat(effect).isEqualTo(NoEffect)
            }

            if (expectRouting) {
                assertThat(routeController.lastResolveRouteLink).isEqualTo(scheduleData.onScheduleItemTap)
                assertThat(routeController.lastResolveRouteLinkOrigin).isNotNull
                assertThat(routeController.lastResolveRouteLinkParams).isEqualTo(mapOf("scheduleItemId" to "1"))
            } else {
                assertThat(routeController.lastResolveRouteLink).isNull()
                assertThat(routeController.lastResolveRouteLinkOrigin).isNull()
                assertThat(routeController.lastResolveRouteLinkParams).isNull()
            }

        }
    }

    @Nested
    @DisplayName("TappedScheduleReminders Action")
    inner class TappedScheduleReminders {

        @Test
        fun withoutLayout_shouldDoNothing() = runTest {
            testFunction(expectShowReminder = false)
        }

        @Test
        fun withLayout_shouldShowReminderUI() = runTest {
            initialState = initialState.copy(layout = mockk())
            testFunction(expectShowReminder = true)
        }

        private fun testFunction(expectShowReminder: Boolean) = runTest {
            scheduleData = createScheduleData(reminders = ScheduleData.Reminders("remindersTappedLink"))
            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.User.TappedScheduleReminders
            ) { state, effect ->
                assertThat(state).isEqualTo(initialState)
                assertThat(effect).isEqualTo(NoEffect)
            }

            if (expectShowReminder) {
                val routeLinkUsed = reminderUIManager.showReminderUICalled
                assertThat(routeLinkUsed).isNotNull
                assertThat(routeLinkUsed).isEqualTo(initialState.layout)
            } else {
                assertThat(reminderUIManager.showReminderUICalled).isNull()
            }
        }
    }

    @Nested
    @DisplayName("TappedEditMyInterests Action")
    inner class TappedEditMyInterests {
        private val editInterestsRouteLink = "editInterestsRouteLink"

        private fun testFunction(shouldSucceed: Boolean) = runTest {
            createReducer()
            reducer.testReduce(initialState, ScheduleAction.User.TappedEditMyInterests) { state, effect ->
                assertThat(state).isEqualTo(initialState)
                assertThat(effect).isEqualTo(NoEffect)
            }

            assertThat(routeController.lastResolveRouteLink != null).isEqualTo(shouldSucceed)
            assertThat(routeController.lastResolveRouteLink == editInterestsRouteLink).isEqualTo(shouldSucceed)
        }

        @Test
        fun withoutLayout_shouldFail() {
            testFunction(shouldSucceed = false)
        }

        @Test
        fun withoutEdit_shouldFail() {
            initialState = initialState.copy(layout = mockk())
            testFunction(shouldSucceed = false)
        }

        @Test
        fun withLayoutAndEdit_shouldSucceed() {
            initialState = initialState.copy(layout = mockk())
            scheduleData = createScheduleData(
                editMyInterests = ScheduleData.EditMyInterests(
                    onTap = editInterestsRouteLink
                )
            )
            testFunction(shouldSucceed = true)
        }
    }

    @Nested
    @DisplayName("ViewSelected Action")
    inner class ViewSelected {

        @Test
        fun selectedList_alreadyHasLoaded_shouldNotLoadAgain() = runTest {
            initialState = initialState.copy(
                selectedView = SelectedView.Timeline,
                list = ViewState.ListState.Content(emptyList())
            )
            testFunction(selectedView = SelectedView.List, shouldLoad = false)
        }

        @Test
        fun selectedTimeline_alreadyHasLoaded_shouldNotLoadAgain() = runTest {
            initialState = initialState.copy(
                selectedView = SelectedView.List,
                timeline = ViewState.TimelineState.Content(emptyList())
            )
            testFunction(selectedView = SelectedView.Timeline, shouldLoad = false)
        }

        @Test
        fun selectedTimeline_notLoaded_shouldLoad() = runTest {
            initialState = initialState.copy(
                selectedView = SelectedView.List,
            )
            testFunction(selectedView = SelectedView.Timeline, shouldLoad = true)
        }

        @Test
        fun selectedList_notLoaded_shouldLoad() = runTest {
            initialState = initialState.copy(
                selectedView = SelectedView.Timeline,
            )
            testFunction(selectedView = SelectedView.List, shouldLoad = true)
        }

        private fun testFunction(
            selectedView: SelectedView,
            shouldLoad: Boolean,
        ) = runTest {
            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.User.ViewSelected(selectedView)
            ) { state, effect ->
                assertThat(state).isEqualTo(initialState.copy(selectedView = selectedView))

                if (shouldLoad) {
                    effect shouldBeAction ScheduleAction.LoadContent(purge = false)
                } else {
                    assertThat(effect).isEqualTo(NoEffect)
                }
            }
        }
    }

    @Nested
    @DisplayName("MyScheduleIdsUpdated Action")
    inner class MyScheduleIdsUpdated {
        @Test
        fun whileInMySchedule_shouldReloadAll() = runTest {
            initialState = initialState.copy(
                selectedSchedule = setOf(SelectedSchedule.MySchedule)
            )

            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.MyScheduleIdsUpdated(setOf(1L))
            ) { state, effect ->
                assertThat(state).isEqualTo(initialState)
                effect shouldBeAction ScheduleAction.LoadItems
            }
        }

        @Test
        fun whileInFullSchedule_shouldUpdateItems() = runTest {
            initialState = initialState.copy(
                selectedSchedule = emptySet()
            )

            val expectedState = initialState.copy()
            mockkStatic(::updateMyScheduleItems)
            every { updateMyScheduleItems(any(), setOf(1L)) } returns expectedState

            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.MyScheduleIdsUpdated(setOf(1L))
            ) { state, effect ->
                assertThat(state).isSameAs(expectedState)
                assertThat(effect).isEqualTo(NoEffect)
            }
        }
    }

    @Nested
    @DisplayName("InterestsUpdated Action")
    inner class InterestsUpdated {
        @Test
        fun whileInMyInterests_shouldReloadAll() = runTest {
            initialState = initialState.copy(
                selectedSchedule = setOf(SelectedSchedule.MyInterests)
            )

            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.InterestsUpdated(setOf("1"))
            ) { state, effect ->
                assertThat(state).isEqualTo(initialState)
                effect shouldBeAction ScheduleAction.LoadItems
            }
        }

        @Test
        fun whileInFullSchedule_shouldDoNothing() = runTest {
            initialState = initialState.copy(
                selectedSchedule = emptySet()
            )

            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.InterestsUpdated(setOf("1"))
            ) { state, effect ->
                assertThat(state).isEqualTo(initialState)
                effect shouldBe NoEffect
            }
        }
    }

    @Nested
    @DisplayName("LoadItems Action")
    inner class InitialLoadItems {
        private val now = ZonedDateTime.now(timezoneProvider.zoneId)
        private val pastHour = now.minusHours(1)
        private val pastDay = now.minusDays(1)
        private val future = now.plusHours(1)

        @Test
        fun differentTimeZone_aggregatedToSame() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = now.withZoneSameInstant(ZoneId.of("Europe/Paris"))),
                TimedScheduleItemGeneratorItem(eventDate = now.withZoneSameInstant(ZoneId.of("America/Phoenix"))),
            )

            val expectedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = now),
                TimedScheduleItemGeneratorItem(eventDate = now),
            )

            testFunction(
                displayMode = DisplayMode.DAILY,
                showPicker = true,
                selectedSchedule = emptySet(),
                itemsAtStart = generatedItems,
                itemsResult = expectedItems,
            )
        }

        @Test
        fun noFavorites_allFuture_displayDaily_fullSchedule_showPicker() {
            //Shouldn't filter out anything
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = future),
                TimedScheduleItemGeneratorItem(eventDate = future),
                TimedScheduleItemGeneratorItem(eventDate = future),
            )

            testFunction(
                displayMode = DisplayMode.DAILY,
                showPicker = true,
                selectedSchedule = emptySet(),
                itemsAtStart = generatedItems,
                itemsResult = generatedItems,
            )
        }

        @Test
        fun withFavorites_allFuture_displayDaily_fullSchedule_showPicker() {
            //FullSchedule selected, favorites has no impact, no filter
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = future),
                TimedScheduleItemGeneratorItem(eventDate = future),
                TimedScheduleItemGeneratorItem(eventDate = future),
            )
            myScheduleManager.addToFavorites(generatedItems.first().scheduleItem)

            testFunction(
                displayMode = DisplayMode.DAILY,
                showPicker = true,
                selectedSchedule = emptySet(),
                itemsAtStart = generatedItems,
                itemsResult = generatedItems,
            )
        }

        @Test
        fun withFavorites_allFuture_displayDaily_mySchedule_showPicker() {
            //MySchedule selected, keep only favorites
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = future),
                TimedScheduleItemGeneratorItem(eventDate = future),
                TimedScheduleItemGeneratorItem(eventDate = future),
            )
            myScheduleManager.addToFavorites(generatedItems.first().scheduleItem)

            testFunction(
                displayMode = DisplayMode.DAILY,
                showPicker = true,
                selectedSchedule = setOf(SelectedSchedule.MySchedule),
                itemsAtStart = generatedItems,
                itemsResult = listOf(generatedItems.first()),
            )
        }

        @Test
        fun noFavorites_somePast_displayMonthly_fullSchedule_showPicker() {
            //Monthly + FullSchedule, filtering past days
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = pastHour),
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
                TimedScheduleItemGeneratorItem(eventDate = future),
            )

            testFunction(
                displayMode = DisplayMode.MONTHLY,
                showPicker = true,
                selectedSchedule = emptySet(),
                itemsAtStart = generatedItems,
                itemsResult = listOf(generatedItems[0], generatedItems[2]),
            )
        }

        @Test
        fun withFavorites_somePast_displayDaily_mySchedule_hidePicker() {
            //MySchedule + hidePicker, filtering myschedule + past days
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = pastHour),
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
                TimedScheduleItemGeneratorItem(eventDate = future),
            )

            generatedItems.forEach {
                myScheduleManager.addToFavorites(it.scheduleItem)
            }

            testFunction(
                displayMode = DisplayMode.DAILY,
                showPicker = false,
                selectedSchedule = setOf(SelectedSchedule.MySchedule),
                itemsAtStart = generatedItems,
                itemsResult = listOf(generatedItems[0], generatedItems[2]),
            )
        }

        @Test
        fun `with Filters, should request with SQL`() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = pastHour),
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
                TimedScheduleItemGeneratorItem(eventDate = future),
            )

            filteringHandler.mockedPredicate = FilteringPredicate.Tag("tag1").query()

            testFunction(
                displayMode = DisplayMode.DAILY,
                showPicker = false,
                selectedSchedule = emptySet(),
                itemsAtStart = generatedItems,
                itemsResult = generatedItems
            )

            timedScheduleItemRepository.lastGetTimedScheduleItemsForTagsQueryPattern shouldBe "tags LIKE '%\"tag1\"%'"
        }

        @Test
        fun `with MyInterests enabled, filled InterestConfig and no interests, should return empty`() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = pastHour),
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
                TimedScheduleItemGeneratorItem(eventDate = future),
            )

            initialState = initialState.copy(selectedSchedule = setOf(SelectedSchedule.MyInterests))

            interestsConfigHolder.currentConfiguration.value = InterestsConfiguration(
                interests = listOf(
                    Interest(id = "0", name = "name0", order = 0, analyticsName = "analyticsName0", tags = listOf("tag0")),
                    Interest(id = "1", name = "name1", order = 1, analyticsName = "analyticsName1", tags = listOf("tag1")),
                ),
            )

            testFunction(
                displayMode = DisplayMode.DAILY,
                showPicker = false,
                selectedSchedule = setOf(SelectedSchedule.MyInterests),
                itemsAtStart = generatedItems,
                itemsResult = emptyList()
            )

            timedScheduleItemRepository.getTimedScheduleItemsForTagsCalled shouldBe false
            timedScheduleItemRepository.lastGetTimedScheduleItemsForTagsQueryPattern shouldBe null
        }

        @Test
        fun `with MyInterests enabled, filled InterestConfig and interests, should request with SQL`() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = pastHour),
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
                TimedScheduleItemGeneratorItem(eventDate = future),
            )

            localStorage.project.interfaceKit.interestIds.value = setOf("0", "1")

            interestsConfigHolder.currentConfiguration.value = InterestsConfiguration(
                interests = listOf(
                    Interest(id = "0", name = "name0", order = 0, analyticsName = "analyticsName0", tags = listOf("tag0", "tag01")),
                    Interest(id = "1", name = "name1", order = 1, analyticsName = "analyticsName1", tags = listOf("tag1")),
                    Interest(id = "2", name = "name2", order = 2, analyticsName = "analyticsName2", tags = listOf("tag2")),
                ),
            )

            testFunction(
                displayMode = DisplayMode.DAILY,
                showPicker = false,
                selectedSchedule = setOf(SelectedSchedule.MyInterests),
                itemsAtStart = generatedItems,
                itemsResult = generatedItems
            )

            timedScheduleItemRepository.lastGetTimedScheduleItemsForTagsQueryPattern shouldBe
                    "tags LIKE '%\"tag0\"%' OR tags LIKE '%\"tag01\"%' OR tags LIKE '%\"tag1\"%'"
        }

        @Test
        fun `with MyInterests disabled, filled InterestConfig and interests, should request empty SQL`() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = pastHour),
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
                TimedScheduleItemGeneratorItem(eventDate = future),
            )

            localStorage.project.interfaceKit.interestIds.value = setOf("0", "1")

            interestsConfigHolder.currentConfiguration.value = InterestsConfiguration(
                interests = listOf(
                    Interest(id = "0", name = "name0", order = 0, analyticsName = "analyticsName0", tags = listOf("tag0", "tag01")),
                    Interest(id = "1", name = "name1", order = 1, analyticsName = "analyticsName1", tags = listOf("tag1")),
                    Interest(id = "2", name = "name2", order = 2, analyticsName = "analyticsName2", tags = listOf("tag2")),
                ),
            )

            testFunction(
                displayMode = DisplayMode.DAILY,
                showPicker = false,
                selectedSchedule = emptySet(),
                itemsAtStart = generatedItems,
                itemsResult = generatedItems
            )

            timedScheduleItemRepository.getTimedScheduleItemsForTagsCalled shouldBe true
            timedScheduleItemRepository.lastGetTimedScheduleItemsForTagsQueryPattern shouldBe null
        }

        @Test
        fun `with MyInterests enabled and filters, filled InterestConfig and interests, should request with SQL`() {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = pastHour),
                TimedScheduleItemGeneratorItem(eventDate = pastDay),
                TimedScheduleItemGeneratorItem(eventDate = future),
            )

            filteringHandler.mockedPredicate = FilteringPredicate.Tag("filter").query()

            initialState = initialState.copy(selectedSchedule = setOf(SelectedSchedule.MyInterests))
            localStorage.project.interfaceKit.interestIds.value = setOf("0", "1")

            interestsConfigHolder.currentConfiguration.value = InterestsConfiguration(
                interests = listOf(
                    Interest(id = "0", name = "name0", order = 0, analyticsName = "analyticsName0", tags = listOf("tag0", "tag01")),
                    Interest(id = "1", name = "name1", order = 1, analyticsName = "analyticsName1", tags = listOf("tag1")),
                    Interest(id = "2", name = "name2", order = 2, analyticsName = "analyticsName2", tags = listOf("tag2")),
                ),
            )

            testFunction(
                displayMode = DisplayMode.DAILY,
                showPicker = false,
                selectedSchedule = setOf(SelectedSchedule.MyInterests),
                itemsAtStart = generatedItems,
                itemsResult = generatedItems
            )

            timedScheduleItemRepository.lastGetTimedScheduleItemsForTagsQueryPattern shouldBe
                    "tags LIKE '%\"filter\"%' AND (tags LIKE '%\"tag0\"%' OR tags LIKE '%\"tag01\"%' OR tags LIKE '%\"tag1\"%')"
        }

        private fun testFunction(
            displayMode: DisplayMode,
            showPicker: Boolean,
            selectedSchedule: Set<SelectedSchedule>,
            itemsAtStart: List<TimedScheduleItem>,
            itemsResult: List<TimedScheduleItem>,
        ) = runTest {
            scheduleData = createScheduleData(
                displayMode = displayMode,
                myFavorites = FavoriteConfig(showPicker = showPicker, emptyPage = EmptyPage("title", "subtitle", "image")),
            )
            initialState = initialState.copy(selectedSchedule = selectedSchedule)
            timedScheduleItemRepository.items = itemsAtStart

            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.LoadItems
            ) { state, effect ->
                assertThat(state).isEqualTo(initialState)
                effect shouldBeAction ScheduleAction.LoadedItemsFromDatabase(itemsResult)
            }
        }
    }

    @Nested
    @DisplayName("LoadedItemsFromDatabase Action")
    inner class LoadedItemsFromDatabase {

        @Test
        fun shouldModifyState_triggerLoadDatePicker() = runTest {
            val generatedItems = generateTimedScheduleItems(
                TimedScheduleItemGeneratorItem(eventDate = ZonedDateTime.now()),
            )
            createReducer()

            reducer.testReduce(
                initialState, ScheduleAction.LoadedItemsFromDatabase(generatedItems)
            ) { state, effect ->
                assertThat(state).isEqualTo(initialState.copy(items = generatedItems))
                effect shouldBeAction ScheduleAction.LoadDatePicker
            }
        }

    }

    @Nested
    @DisplayName("LoadDatePicker Action")
    inner class InitialLoadDatePicker {
        @Test
        fun shouldModifyState_triggerLoadContent() = runTest {
            val expectedState = initialState.copy()
            mockkStatic(::loadDatePicker)
            every { loadDatePicker(any(), any()) } returns expectedState

            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.LoadDatePicker
            ) { state, effect ->
                assertThat(state).isSameAs(expectedState)
                effect shouldBeAction ScheduleAction.LoadContent(purge = true)
            }
        }
    }

    @Nested
    @DisplayName("LoadContent Action")
    inner class InitialLoadContent {
        @Test
        fun withoutWidgets_shouldModifyState() = runTest {
            val paramPurge = slot<Boolean>()
            val paramWidgets = slot<LinkedHashMap<Int, List<WidgetCollectionView.WidgetItem>>>()

            val expectedState = initialState.copy()
            mockkStatic(::loadContent)
            every {
                loadContent(
                    any(),
                    capture(paramPurge),
                    any(),
                    any(),
                    capture(paramWidgets),
                    any(),
                    any(),
                    any()
                )
            } returns expectedState

            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.LoadContent(purge = true)
            ) { state, effect ->
                assertThat(state).isSameAs(expectedState)
                assertThat(effect).isEqualTo(NoEffect)
            }

            assertThat(paramPurge.captured).isTrue
            assertThat(paramWidgets.captured).isEqualTo(linkedMapOf<Int, List<WidgetCollectionView.WidgetItem>>())
        }

        @Test
        fun withWidgets_shouldModifyState() = runTest {
            val paramPurge = slot<Boolean>()
            val paramWidgets = slot<LinkedHashMap<Int, List<WidgetCollectionView.WidgetItem>>>()

            val widgetCollectionConfigurationInstance = WidgetCollectionConfiguration.Instance(
                widgets = listOf(
                    WidgetCollectionConfiguration.Instance.WidgetInfo(
                        WidgetCollectionConfiguration.Instance.WidgetKey(name = "testKey", version = 1),
                        JsonArray(emptyList()),
                    )
                ),
            )
            val widgetCollectionList = listOf(
                WidgetCollectionCellLayoutData(
                    index = 1,
                    collection = widgetCollectionConfigurationInstance
                )
            )
            scheduleData = createScheduleData(widgetCollections = widgetCollectionList)

            val expectedState = initialState.copy()
            mockkStatic(::loadContent)
            every {
                loadContent(
                    any(),
                    capture(paramPurge),
                    any(),
                    any(),
                    capture(paramWidgets),
                    any(),
                    any(),
                    any()
                )
            } returns expectedState

            createReducer()
            reducer.testReduce(
                initialState, ScheduleAction.LoadContent(purge = false)
            ) { state, effect ->
                assertThat(state).isSameAs(expectedState)
                assertThat(effect).isEqualTo(NoEffect)
            }

            assertThat(paramPurge.captured).isFalse
            val widgets1 = paramWidgets.captured
            assertThat(widgets1).isNotEmpty

            //Second check to verify that widgets are not resolved again
            reducer.testReduce(
                initialState, ScheduleAction.LoadContent(purge = false)
            ) { _, _ -> }

            val widgets2 = paramWidgets.captured
            assertThat(widgets1).isSameAs(widgets2)
        }

    }

    private fun createReducer() {
        reducer = ScheduleReducer(
            timedScheduleItemRepository,
            myScheduleManager,
            localizationService,
            filteringHandler,
            eventConfigHolder,
            interestsConfigHolder,
            timezoneProvider,
            widgetResolver,
            scheduleData,
            routeController,
            reminderUIManager,
            localStorage,
            conditionChecker,
            App.resolve(),
        )
    }
}
