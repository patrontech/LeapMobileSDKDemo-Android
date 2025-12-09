package com.greencopper.interfacekit.onboarding.pages

import com.greencopper.core.conditions.ConditionSet
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.location.service.LocationService
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.core.MockBluetoothService
import com.greencopper.testmocks.core.MockNotificationPermissionService
import com.greencopper.testmocks.interfacekit.MockCommand
import com.greencopper.testmocks.interfacekit.MockCommandExecutor
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class ConcreteOnboardingPageActionHandlerTest {

    private lateinit var classUnderTest: ConcreteOnboardingPageActionHandler
    private val mockRouteController: RouteController = mockk()
    private val mockCommandExecutor = MockCommandExecutor()
    private val mockConditionChecker: ConditionChecker = mockk()
    private val mockMetricsService: AggregateMetricsService = mockk()
    private val mockLocationService: LocationService = mockk()
    private val mockBluetoothService = MockBluetoothService()
    private val buildConfigProvider = MockBuildConfigProvider()
    private val notificationPermissionService = MockNotificationPermissionService()
    private lateinit var mockRemoteStateDispatcher: MockRemoteStateDispatcher

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
        mockRemoteStateDispatcher = MockRemoteStateDispatcher(json = App.resolve())
        every { mockMetricsService.track(any()) } returns Unit
        classUnderTest =
            ConcreteOnboardingPageActionHandler(
                LazyResolver.adhoc(mockRouteController),
                LazyResolver.adhoc(mockCommandExecutor),
                LazyResolver.adhoc(mockConditionChecker),
                LazyResolver.adhoc(mockMetricsService),
                LazyResolver.adhoc(mockLocationService),
                LazyResolver.adhoc(mockBluetoothService),
                LazyResolver.adhoc(notificationPermissionService),
                LazyResolver.adhoc(buildConfigProvider),
                LazyResolver.adhoc(mockRemoteStateDispatcher),
            )
    }

    @Test
    @DisplayName("Given action is complete with true, When calling executeAction, Then true is returned")
    fun executeActionCompleteShouldReturnTrue() {
        val action = OnboardingPageAction.Complete(true, "complete")
        runTest {
            assertThat(classUnderTest.executeAction(action, mockk())).isTrue
            verify { mockMetricsService.track(any()) }
        }
    }

    @Test
    @DisplayName("Given action is complete with false, When calling executeAction, Then false is returned")
    fun executeActionCompleteShouldReturnFalse() {
        val action = OnboardingPageAction.Complete(false, "complete")
        runTest {
            assertThat(classUnderTest.executeAction(action, mockk())).isFalse
            verify { mockMetricsService.track(any()) }
        }
    }

    @Test
    @DisplayName("Given action is present and data is valid and action is completed, When calling executeAction, Then true is returned")
    fun executeActionPresentShouldReturnTrue() {
        val action = OnboardingPageAction.Present(mockk(), mockk(), "present")
        every { mockRouteController.resolve(any(), any()) } returns Unit
        runTest {
            every { mockConditionChecker.checkFlow(any<ConditionSet>()) } returns flowOf(
                false,
                true
            )
            assertThat(classUnderTest.executeAction(action, mockk())).isTrue
            verify { mockMetricsService.track(any()) }
        }
    }

    @Test
    @DisplayName("Given action is command and data is valid, When calling executeAction, Then true is returned")
    fun executeActionCommandShouldReturnTrue() {
        val action = OnboardingPageAction.Execute(MockCommand.commandInfo)
        every { mockRouteController.resolve(any(), any()) } returns Unit
        runTest {
            assertThat(classUnderTest.executeAction(action, mockk())).isTrue
        }
    }

    @Test
    fun commandThrows_executeActionCommand_shouldReturnFalse() {
        mockCommandExecutor.shouldThrow = true
        val action = OnboardingPageAction.Execute(MockCommand.commandInfo)

        runTest {
            assertThat(classUnderTest.executeAction(action, mockk())).isFalse
        }
    }

    @Test
    @DisplayName("Given action is present and data is valid and action is not completed, When calling executeAction, Then false is returned")
    fun executeActionPresentShouldReturnFalse() {
        val action = OnboardingPageAction.Present(mockk(), mockk(), "present")
        every { mockRouteController.resolve(any(), any()) } returns Unit
        runTest {
            every { mockConditionChecker.checkFlow(any<ConditionSet>()) } returns flowOf(
                false,
                false
            )
            assertThat(classUnderTest.executeAction(action, mockk())).isFalse
            verify { mockMetricsService.track(any()) }
        }
    }

    @Test
    @DisplayName("Given action is location permission, When calling executeAction, Then true should be returned")
    fun executeActionLocationShouldReturnTrue() {
        val action = OnboardingPageAction.LocationPermission("whenInUse", "location_permission")
        runTest {
            val origin: Layout = mockk()
            every { origin.activity } returns mockk()
            every {
                mockLocationService.requestPermissions(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns flowOf(true)
            every {
                mockLocationService.getAuthorizationStatus()
            } returns AuthorizationStatus.AuthorizedWhenInUse
            assertThat(classUnderTest.executeAction(action, origin)).isTrue
            verify { mockMetricsService.track(any()) }
            mockRemoteStateDispatcher.dispatchCallCount shouldBe 1
            mockRemoteStateDispatcher.dispatchedEntry?.key shouldBe "location_permission"
        }
    }

    @Test
    @DisplayName("Given action is location permission and origin activity is null, When calling executeAction, Then true should be returned")
    fun executeActionNullActivityLocationShouldReturnTrue() {
        val action = OnboardingPageAction.LocationPermission("whenInUse", "location_permission")
        runTest {
            val origin: Layout = mockk()
            every { origin.activity } returns null
            every {
                mockLocationService.requestPermissions(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns flowOf(true)
            verify(inverse = true) { mockMetricsService.track(any()) }
            assertThat(classUnderTest.executeAction(action, origin)).isTrue
        }
    }

    @Test
    @DisplayName("Given action is bluetooth permission, When calling executeAction, Then true should be returned")
    fun executeActionBluetoothShouldReturnTrue() {
        val action = OnboardingPageAction.BluetoothPermission("request", "bluetooth_permission")
        runTest {
            val origin: Layout = mockk()
            every { origin.activity } returns mockk()
            assertThat(classUnderTest.executeAction(action, origin)).isTrue
            verify { mockMetricsService.track(any()) }
        }
    }

    @Test
    @DisplayName("Given action is bluetooth permission and origin activity is null, When calling executeAction, Then true should be returned")
    fun executeActionBluetoothNullActivityShouldReturnTrue() {
        val action = OnboardingPageAction.BluetoothPermission("request", "bluetooth_permission")
        runTest {
            val origin: Layout = mockk()
            every { origin.activity } returns null
            assertThat(classUnderTest.executeAction(action, origin)).isTrue
            verify(inverse = true) { mockMetricsService.track(any()) }
        }
    }

    @Test
    @DisplayName("Given action doesn't provide analytics event, When calling executeAction, Then track is not called")
    fun executeActionShouldNotCallTrack() {
        val action = OnboardingPageAction.Complete(true)
        runTest {
            classUnderTest.executeAction(action, mockk())
            verify(inverse = true) { mockMetricsService.track(any()) }
        }
    }

    @Test
    @DisplayName("Given action is present and route controller throws an exception, When calling executeAction, Then false is returned")
    fun executeActionPresentWithExceptionShouldReturnFalse() {
        val action = OnboardingPageAction.Present(mockk(), mockk(), "present")
        every { mockRouteController.resolve(any(), any()) } throws RuntimeException()
        runTest {
            every { mockConditionChecker.checkFlow(any<ConditionSet>()) } returns flowOf(
                false,
                true
            )
            assertThat(classUnderTest.executeAction(action, mockk())).isFalse
        }
    }

    @Test
    @DisplayName("Given ActionClickEvent with valid eventName, When calling track, Then provider is called")
    fun providerTrackIsCalled() {
        val actionClickEvent = ConcreteOnboardingPageActionHandler.ActionClickEvent("event")
        val mockMappedProvider: MappedProvider = mockk()
        every { mockMappedProvider.track(any(), any()) } returns Unit
        actionClickEvent.track(mockMappedProvider)
        verify { mockMappedProvider.track(any(), any()) }
    }

    @Test
    @DisplayName("Given action is notification permission on SDK API 33, When calling executeAction, Then true should be returned")
    fun executeActionNotificationPermissionShouldReturnTrue() {
        buildConfigProvider.mockSdkInt = 33
        val action = OnboardingPageAction.NotificationPermission("notification_permission")
        runTest {
            val origin: Layout = mockk()
            every { origin.activity } returns mockk()
            assertThat(classUnderTest.executeAction(action, origin)).isTrue
            verify { mockMetricsService.track(any()) }
            mockRemoteStateDispatcher.dispatchCallCount shouldBe 1
            mockRemoteStateDispatcher.dispatchedEntry?.key shouldBe "notification_permission"
        }
    }

    @Test
    @DisplayName("Given action is notification permission on SDK API 30, When calling executeAction, Then false should be returned")
    fun executeActionNotificationPermissionShouldReturnFalse() {
        buildConfigProvider.mockSdkInt = 30
        val action = OnboardingPageAction.NotificationPermission("notification_permission")
        runTest {
            val origin: Layout = mockk()
            every { origin.activity } returns mockk()
            assertThat(classUnderTest.executeAction(action, origin)).isFalse
            mockRemoteStateDispatcher.dispatchCallCount shouldBe 1
            mockRemoteStateDispatcher.dispatchedEntry?.key shouldBe "notification_permission"
        }
    }

    @Test
    @DisplayName("Given action is notification permission, When calling executeAction and activity is null, Then false should be returned")
    fun executeActionNotificationPermissionAndActivityIsNullShouldReturnTrue() {
        buildConfigProvider.mockSdkInt = 33
        val action = OnboardingPageAction.NotificationPermission("notification_permission")
        runTest {
            val origin: Layout = mockk()
            every { origin.activity } returns null
            assertThat(classUnderTest.executeAction(action, origin)).isFalse
        }
    }

    @Test
    @DisplayName("Given action is notification permission, When calling executeAction and analyticsEvent is null, Then true should be returned")
    fun executeActionNotificationPermissionAndAnalyticsEventIsNullShouldReturnTrue() {
        buildConfigProvider.mockSdkInt = 33
        val action = OnboardingPageAction.NotificationPermission()
        runTest {
            val origin: Layout = mockk()
            every { origin.activity } returns mockk()
            assertThat(classUnderTest.executeAction(action, origin)).isTrue
            mockRemoteStateDispatcher.dispatchCallCount shouldBe 1
            mockRemoteStateDispatcher.dispatchedEntry?.key shouldBe "notification_permission"
        }
    }
}
