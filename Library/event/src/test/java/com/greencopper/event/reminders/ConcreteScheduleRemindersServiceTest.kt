package com.greencopper.event.reminders

import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.event.common.EventProjectLocalStorageDomain
import com.greencopper.event.common.event
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.recipe.EventConfiguration
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.event.scheduleItem.ScheduleItem
import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.eventmocks.MockMyScheduleManager
import com.greencopper.eventmocks.MockTimedScheduleItemRepository
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.*
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class ConcreteScheduleRemindersServiceTest: CoroutineTest(UnconfinedTestDispatcher()) {

    private val mockWorkManager: WorkManager = mockk()
    private val lazyLocalStorage: LazyResolver<LocalStorage> by lazy { LazyResolver.adhoc(App.resolve()) }
    private val eventLocalStorage: EventProjectLocalStorageDomain
            get() = lazyLocalStorage.resolve().project.event

    private val mockProjectTagProvider = object : CurrentProjectTagProvider {
        override var currentProject: String? = "testproject"
        override val currentProjectFlow: SharedFlow<String?>
            get() = MutableStateFlow(currentProject)
    }
    private val configHolder = EventConfigurationHolder()
    private val mockTimedScheduleItemRepository = MockTimedScheduleItemRepository(listOf())
    private val mockNotificationPermissionService = MockNotificationPermissionService()

    private val reminderService = ConcreteScheduleRemindersService(
        workManager = mockWorkManager,
        scope = testScope,
        currentProjectTagProvider = mockProjectTagProvider,
        timezoneProvider = MockTimezoneProvider(),
        localizationService = MockLocalizationService(),
        eventConfigHolder = configHolder,
        timedScheduleItemRepository = mockTimedScheduleItemRepository,
        lazyLocalStorage = lazyLocalStorage,
        notificationPermissionService = mockNotificationPermissionService,
        myScheduleManager = MockMyScheduleManager(setOf()),
    )

    init {
        Toolkit.setupTest()
        every { mockWorkManager.cancelAllWorkByTag(any()) } returns mockk()
        every { mockWorkManager.enqueue(any<WorkRequest>()) } returns mockk()
        eventLocalStorage.reminderIntervalMins.value = 10
        configHolder.currentConfiguration.value = EventConfiguration(
            reminders = EventConfiguration.Reminders(
                topBarIcon = "",
                timeIntervals = listOf(EventConfiguration.TimeInterval("10", 10, "message")),
                defaultTimeInterval = 10,
                onFirstAddToMyScheduleRouteLink = "",
                onNotificationTapRouteLink = "",
            )
        )
    }

    override fun afterEach() {}

    @Test
    fun setReminderInterval_changesInterval() {
        reminderService.setReminderInterval(20)
        assertThat(eventLocalStorage.reminderIntervalMins.value).isEqualTo(20)
    }

    @Test
    fun givenCurrentProjectEmpty_getTimedScheduleItems_notCalled() {
        mockProjectTagProvider.currentProject = null

        runTest {
            reminderService.collectScheduleReminders()
            assertThat(mockTimedScheduleItemRepository.getTimedScheduleItemsForScheduleItemIdsCalled).isFalse
        }
    }

    @Test
    fun givenNotificationPermissionNotAuthorized_getTimedScheduleItems_notCalled() {
        mockNotificationPermissionService.authorizationStatusMock = AuthorizationStatus.NotDetermined

        runTest {
            reminderService.collectScheduleReminders()
            assertThat(mockTimedScheduleItemRepository.getTimedScheduleItemsForScheduleItemIdsCalled).isFalse
        }
    }

    @Test
    fun givenNoInterval_getTimedScheduleItems_notCalled() {
        lazyLocalStorage.resolve().project.event.reminderIntervalMins.value = null

        runTest {
            reminderService.collectScheduleReminders()
            assertThat(mockTimedScheduleItemRepository.getTimedScheduleItemsForScheduleItemIdsCalled).isFalse
        }
    }

    @Test
    fun givenInvalidInterval_getTimedScheduleItems_notCalled() {
        configHolder.currentConfiguration.value = null
        lazyLocalStorage.resolve().project.event.reminderIntervalMins.value = -1

        runTest {
            reminderService.collectScheduleReminders()
            assertThat(mockTimedScheduleItemRepository.getTimedScheduleItemsForScheduleItemIdsCalled).isFalse
        }
    }

    @Test
    fun givenValidState_getTimedScheduleItems_isCalled() {
        runTest {
            reminderService.collectScheduleReminders()
            assertThat(mockTimedScheduleItemRepository.getTimedScheduleItemsForScheduleItemIdsCalled).isTrue
        }
    }

    @Test
    fun givenEventInThePast_enqueue_notCalled() {
        val timedScheduleItem = TimedScheduleItem(
            scheduleItem = ScheduleItem(
                itemId = 1L,
                activityId = 1L,
                name = "name",
                photos = listOf(),
            ),
            timeSlot = TimeSlot(
                id = 1L,
                scheduleItemId = 1L,
                dayOfEvent = ZonedDateTime.now(),
                startDate = ZonedDateTime.now().minusDays(1)
            )
        )
        mockTimedScheduleItemRepository.items = listOf(timedScheduleItem)
        eventLocalStorage.myScheduleItemIds.value = setOf(timedScheduleItem.scheduleItem.itemId)

        runTest {
            reminderService.collectScheduleReminders()
            verify(exactly = 0) { mockWorkManager.enqueue(any<WorkRequest>()) }
        }
    }

    @Test
    fun givenNotificationScheduledInThePast_enqueue_notCalled() {
        val timedScheduleItem = TimedScheduleItem(
            scheduleItem = ScheduleItem(
                itemId = 1L,
                activityId = 1L,
                name = "name",
                photos = listOf(),
            ),
            timeSlot = TimeSlot(
                id = 1L,
                scheduleItemId = 1L,
                dayOfEvent = ZonedDateTime.now(),
                startDate = ZonedDateTime.now().plusMinutes(5)
            )
        )
        mockTimedScheduleItemRepository.items = listOf(timedScheduleItem)
        eventLocalStorage.myScheduleItemIds.value = setOf(timedScheduleItem.scheduleItem.itemId)

        runTest {
            reminderService.collectScheduleReminders()
            verify(exactly = 0) { mockWorkManager.enqueue(any<WorkRequest>()) }
        }
    }

    @Test
    fun givenEventNoStartDate_enqueue_notCalled() {
        val timedScheduleItem = TimedScheduleItem(
            scheduleItem = ScheduleItem(
                itemId = 1L,
                activityId = 1L,
                name = "name",
                photos = listOf(),
            ),
            timeSlot = TimeSlot(
                id = 1L,
                scheduleItemId = 1L,
                dayOfEvent = ZonedDateTime.now(),
                startDate = null,
            )
        )
        mockTimedScheduleItemRepository.items = listOf(timedScheduleItem)
        eventLocalStorage.myScheduleItemIds.value = setOf(timedScheduleItem.scheduleItem.itemId)

        runTest {
            reminderService.collectScheduleReminders()
            verify(exactly = 0) { mockWorkManager.enqueue(any<WorkRequest>()) }
        }
    }

    @Test
    fun givenEventFutureStartDate_enqueue_isCalled() {
        configHolder.currentConfiguration.value = EventConfiguration(
            reminders = EventConfiguration.Reminders(
                topBarIcon = "",
                timeIntervals = listOf(
                    EventConfiguration.TimeInterval("10", 10, "message"),
                ),
                defaultTimeInterval = 1,
                onFirstAddToMyScheduleRouteLink = "",
                onNotificationTapRouteLink = ""
            )
        )

        val timedScheduleItem = TimedScheduleItem(
            scheduleItem = ScheduleItem(
                itemId = 1L,
                activityId = 1L,
                name = "name",
                photos = listOf(),
            ),
            timeSlot = TimeSlot(
                id = 1L,
                scheduleItemId = 1L,
                dayOfEvent = ZonedDateTime.now(),
                startDate = ZonedDateTime.now().plusDays(1),
            )
        )
        mockTimedScheduleItemRepository.items = listOf(timedScheduleItem)
        eventLocalStorage.myScheduleItemIds.value = setOf(timedScheduleItem.scheduleItem.itemId)

        runTest {
            reminderService.collectScheduleReminders()
            verify { mockWorkManager.enqueue(any<WorkRequest>()) }
        }
    }

    @Test
    fun givenNoEventConfig_enqueue_notCalled() {
        configHolder.currentConfiguration.value = null
        val timedScheduleItem = TimedScheduleItem(
            scheduleItem = ScheduleItem(
                itemId = 1L,
                activityId = 1L,
                name = "name",
                photos = listOf(),
            ),
            timeSlot = TimeSlot(
                id = 1L,
                scheduleItemId = 1L,
                dayOfEvent = ZonedDateTime.now(),
                startDate = ZonedDateTime.now().plusDays(1),
            )
        )
        mockTimedScheduleItemRepository.items = listOf(timedScheduleItem)
        eventLocalStorage.myScheduleItemIds.value = setOf(timedScheduleItem.scheduleItem.itemId)

        runTest {
            reminderService.collectScheduleReminders()
            verify(exactly = 0) { mockWorkManager.enqueue(any<WorkRequest>()) }
        }
    }

    @Test
    fun givenIntervalNotInValidIntervals_changesTo_higherInterval() {
        configHolder.currentConfiguration.value = EventConfiguration(
            reminders = EventConfiguration.Reminders(
                topBarIcon = "",
                timeIntervals = listOf(
                    EventConfiguration.TimeInterval("1", 1, "message"),
                    EventConfiguration.TimeInterval("2", 2, "message"),
                    EventConfiguration.TimeInterval("15", 15, "message"),
                ),
                defaultTimeInterval = 1,
                onFirstAddToMyScheduleRouteLink = "",
                onNotificationTapRouteLink = ""
            )
        )

        runTest {
            reminderService.collectScheduleReminders()
            assertThat(eventLocalStorage.reminderIntervalMins.value).isEqualTo(15)
        }
    }

    @Test
    fun givenIntervalNotInValidIntervals_changesTo_highestInterval() {
        configHolder.currentConfiguration.value = EventConfiguration(
            reminders = EventConfiguration.Reminders(
                topBarIcon = "",
                timeIntervals = listOf(
                    EventConfiguration.TimeInterval("1", 1, "message"),
                    EventConfiguration.TimeInterval("2", 2, "message"),
                    EventConfiguration.TimeInterval("5", 5, "message"),
                ),
                defaultTimeInterval = 1,
                onFirstAddToMyScheduleRouteLink = "",
                onNotificationTapRouteLink = ""
            )
        )

        runTest {
            reminderService.collectScheduleReminders()
            assertThat(eventLocalStorage.reminderIntervalMins.value).isEqualTo(5)
        }
    }

    @Test
    fun givenNoIntervals_changesTo_invalidInterval() {
        configHolder.currentConfiguration.value = EventConfiguration(
            reminders = EventConfiguration.Reminders(
                topBarIcon = "",
                timeIntervals = listOf(),
                defaultTimeInterval = 1,
                onFirstAddToMyScheduleRouteLink = "",
                onNotificationTapRouteLink = ""
            )
        )

        runTest {
            reminderService.collectScheduleReminders()
            assertThat(eventLocalStorage.reminderIntervalMins.value).isEqualTo(-1)
        }
    }

    @Test
    fun givenIntervalInIntervals_interval_doesntChange() {
        configHolder.currentConfiguration.value = EventConfiguration(
            reminders = EventConfiguration.Reminders(
                topBarIcon = "",
                timeIntervals = listOf(
                    EventConfiguration.TimeInterval("5", 5, "message"),
                    EventConfiguration.TimeInterval("10", 10, "message"),
                    EventConfiguration.TimeInterval("15", 15, "message"),
                ),
                defaultTimeInterval = 1,
                onFirstAddToMyScheduleRouteLink = "",
                onNotificationTapRouteLink = ""
            )
        )

        runTest {
            reminderService.collectScheduleReminders()
            assertThat(eventLocalStorage.reminderIntervalMins.value).isEqualTo(10)
        }
    }
}
