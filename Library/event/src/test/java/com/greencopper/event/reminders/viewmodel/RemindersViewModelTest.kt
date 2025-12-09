package com.greencopper.event.reminders.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.coremocks.MockNotificationManagerClient
import com.greencopper.event.common.event
import com.greencopper.event.recipe.EventConfiguration
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.event.recipe.NO_REMINDERS_INTERVAL
import com.greencopper.eventmocks.MockScheduleRemindersService
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.core.MockNotificationPermissionService
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class RemindersViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val eventConfigurationHolder = EventConfigurationHolder()
    private val localStorage: LocalStorage

    init {
        Toolkit.setupTest(applicationContext = context)
        localStorage = App.resolve()
    }

    @AfterEach
    fun afterEach() {
        unmockkAll()
    }

    private val scheduleRemindersService = MockScheduleRemindersService()
    private val buildConfigProvider = MockBuildConfigProvider()
    private val notificationPermissionService = MockNotificationPermissionService()
    private val notificationManager = MockNotificationManagerClient(
        getNotificationChannelAction = {
            val notificationChannel: NotificationChannel = mockk(relaxed = true)
            every { notificationChannel.importance } returns NotificationManager.IMPORTANCE_DEFAULT
            notificationChannel
        }
    )

    private val testIntervals = listOf(
        EventConfiguration.TimeInterval(
            "label",
            10
        )
    )

    private val testReminders = EventConfiguration.Reminders(
        "topBarIcon",
        testIntervals,
        10,
        "onFirstAddToMyScheduleRouteLink",
        "onNotificationTapRouteLink",
    )

    private val classUnderTest by lazy {
        RemindersViewModel(
            eventConfigurationHolder,
            LazyResolver.adhoc(localStorage),
            scheduleRemindersService,
            notificationPermissionService,
            MockLocalizationService(),
            notificationManager
        )
    }

    @Test
    fun getIntervals_withoutConfiguration_shouldReturnEmptyList() {
        assertThat(classUnderTest.getIntervals()).isEmpty()
    }

    @Test
    fun getIntervals_withoutNullConfiguration_shouldReturnEmptyList() {
        eventConfigurationHolder.currentConfiguration.value = EventConfiguration()
        assertThat(classUnderTest.getIntervals()).isEmpty()
    }

    @Test
    fun getIntervals_withConfiguration_shouldReturnTestIntrvals() {
        eventConfigurationHolder.currentConfiguration.value = EventConfiguration(testReminders)
        assertThat(classUnderTest.getIntervals()).usingRecursiveComparison().isEqualTo(testIntervals)
    }

    @Test
    fun getDefaultInterval_whenNotificationsDisabled_AndAuthorizationStatusDenied_AndNoConfiguration_shouldReturnNoReminders() {
        notificationManager.areNotificationsEnabledAction = { false }
        eventConfigurationHolder.currentConfiguration.value = null
        notificationPermissionService.authorizationStatusMock = AuthorizationStatus.Denied
        assertThat(classUnderTest.getDefaultInterval(context)).isEqualTo(NO_REMINDERS_INTERVAL)
    }

    @Test
    fun getDefaultInterval_whenNotificationsEnabled_AndAuthorizationStatusDenied_AndNoConfiguration_shouldReturnNoReminders() {
        notificationManager.areNotificationsEnabledAction = { true }
        eventConfigurationHolder.currentConfiguration.value = null
        notificationPermissionService.authorizationStatusMock = AuthorizationStatus.Denied
        assertThat(classUnderTest.getDefaultInterval(context)).isEqualTo(NO_REMINDERS_INTERVAL)
    }

    @Test
    fun getDefaultInterval_whenNotificationsDisabled_AndAuthorizationStatusAuthorizedAlways_AndNoConfiguration_shouldReturnNoReminders() {
        notificationManager.areNotificationsEnabledAction = { false }
        eventConfigurationHolder.currentConfiguration.value = null
        notificationPermissionService.authorizationStatusMock = AuthorizationStatus.AuthorizedAlways
        assertThat(classUnderTest.getDefaultInterval(context)).isEqualTo(NO_REMINDERS_INTERVAL)
    }

    @Test
    fun getDefaultInterval_whenNotificationsEnabled_AndAuthorizationStatusAuthorizedAlways_AndNoConfiguration_shouldReturnNoReminders() {
        notificationManager.areNotificationsEnabledAction = { true }
        eventConfigurationHolder.currentConfiguration.value = null
        notificationPermissionService.authorizationStatusMock = AuthorizationStatus.AuthorizedAlways
        assertThat(classUnderTest.getDefaultInterval(context)).isEqualTo(NO_REMINDERS_INTERVAL)
    }

    @Test
    fun getDefaultInterval_whenNotificationsEnabled_AndAuthorizationStatusAuthorizedAlways_AndEmptyConfiguration_shouldReturnNoReminders() {
        notificationManager.areNotificationsEnabledAction = { true }
        eventConfigurationHolder.currentConfiguration.value = EventConfiguration()
        notificationPermissionService.authorizationStatusMock = AuthorizationStatus.AuthorizedAlways
        assertThat(classUnderTest.getDefaultInterval(context)).isEqualTo(NO_REMINDERS_INTERVAL)
    }

    @Test
    fun getDefaultInterval_whenNotificationsEnabled_AndAuthorizationStatusAuthorizedAlways_AndConfigurationExists_shouldDefaultTimeInterval() {
        notificationManager.areNotificationsEnabledAction = { true }
        eventConfigurationHolder.currentConfiguration.value = EventConfiguration(testReminders)
        notificationPermissionService.authorizationStatusMock = AuthorizationStatus.AuthorizedAlways
        assertThat(classUnderTest.getDefaultInterval(context)).isEqualTo(testReminders.defaultTimeInterval)
    }

    @Test
    fun getDefaultInterval_whenNotificationsDisabled_AndAuthorizationStatusDenied_shouldReturnNoReminders() {
        notificationManager.areNotificationsEnabledAction = { true }
        eventConfigurationHolder.currentConfiguration.value = EventConfiguration(testReminders)
        notificationPermissionService.authorizationStatusMock = AuthorizationStatus.AuthorizedAlways
        val reminderIntervalMin = 100
        localStorage.project.event.reminderIntervalMins.value = reminderIntervalMin
        assertThat(classUnderTest.getDefaultInterval(context)).isEqualTo(reminderIntervalMin)
    }

    @Test
    fun removeReminders_shouldSetToNoRemonders() {
        classUnderTest.removeReminders()
        assertThat(scheduleRemindersService.currentReminderInterval).isEqualTo(NO_REMINDERS_INTERVAL)
    }

    @Test
    fun setRemindersInterval() {
        val interval = 10
        classUnderTest.setRemindersInterval(interval)
        assertThat(scheduleRemindersService.currentReminderInterval).isEqualTo(interval)
    }

    @Test
    fun areNotificationsEnabled_whenNotificationsDisabled_shouldReturnFalse() {
        notificationManager.areNotificationsEnabledAction = { false }
        assertThat(classUnderTest.areNotificationsEnabled(context)).isFalse
    }

    @Test
    fun areNotificationsEnabled_whenNotificationsEnabled_moreThanO_withImportanceNone_shouldReturnFalse() {
        notificationManager.areNotificationsEnabledAction = { true }
        notificationManager.getNotificationChannelAction = {
            val notificationChannel: NotificationChannel = mockk(relaxed = true)
            every { notificationChannel.importance } returns NotificationManager.IMPORTANCE_NONE
            notificationChannel
        }
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.O
        every { context.getString(any()) } returns ""
        assertThat(classUnderTest.areNotificationsEnabled(context)).isFalse
    }

    @Test
    fun getNotificationAuthorizationStatus_shouldReturn() {
        assertThat(classUnderTest.getNotificationsAuthorizationStatus())
            .isEqualTo(AuthorizationStatus.AuthorizedAlways)
    }

    @Test
    fun getNotificationRequestPermission_T() {
        val activity: FragmentActivity = mockk()
        notificationManager.areNotificationsEnabledAction = { true }
        every { activity.getString(any()) } returns ""
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.TIRAMISU
        runTest {
            assertThat(classUnderTest.requestNotificationPermission(activity)).isTrue()
        }
    }

    @Test
    fun getNotificationRequestPermission_lessThanT() {
        notificationManager.areNotificationsEnabledAction = { false }
        val activity: FragmentActivity = mockk()
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.TIRAMISU - 1
        notificationPermissionService.resultFromSettings = true
        runTest {
            assertThat(classUnderTest.requestNotificationPermission(activity)).isTrue()
        }
    }
}
