package com.greencopper.core.permissions.notification.service

import android.Manifest
import android.content.Intent
import android.os.Build.VERSION_CODES
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.testmocks.core.MockPermissionManager
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConcreteNotificationPermissionServiceTest {
    private val buildConfigProvider = MockBuildConfigProvider()
    private val testPermissionManager = MockPermissionManager()
    private val classUnderTest = ConcreteNotificationPermissionService(testPermissionManager, buildConfigProvider)
    private val activity: FragmentActivity = mockk()

    @Test
    fun check_GetAuthorizationStatus_WhenTiramisuAndHasPermission() {
        buildConfigProvider.mockSdkInt = VERSION_CODES.TIRAMISU
        testPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.POST_NOTIFICATIONS] = true
        assertThat(classUnderTest.getAuthorizationStatus())
            .isEqualTo(AuthorizationStatus.AuthorizedAlways)
    }

    @Test
    fun check_GetAuthorizationStatus_WhenTiramisuAndNoPermissionAndNotRequested() {
        buildConfigProvider.mockSdkInt = VERSION_CODES.TIRAMISU
        testPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.POST_NOTIFICATIONS] = false
        testPermissionManager.alreadyRequestedPermission = emptySet()
        assertThat(classUnderTest.getAuthorizationStatus())
            .isEqualTo(AuthorizationStatus.NotDetermined)
    }

    @Test
    fun check_GetAuthorizationStatus_WhenTiramisuAndNoPermissionAndRequested() {
        buildConfigProvider.mockSdkInt = VERSION_CODES.TIRAMISU
        testPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.POST_NOTIFICATIONS] = false
        testPermissionManager.alreadyRequestedPermission = setOf(Manifest.permission.POST_NOTIFICATIONS)
        assertThat(classUnderTest.getAuthorizationStatus())
            .isEqualTo(AuthorizationStatus.Denied)
    }

    @Test
    fun check_GetAuthorizationStatus_WhenLowerTiramisuAndNoPermissionAndRequested() {
        buildConfigProvider.mockSdkInt = VERSION_CODES.TIRAMISU - 1
        assertThat(classUnderTest.getAuthorizationStatus())
            .isEqualTo(AuthorizationStatus.AuthorizedAlways)
    }

    @Test
    fun check_GetAuthorizationStatusFlow() {
        buildConfigProvider.mockSdkInt = VERSION_CODES.TIRAMISU - 1
        runTest {
            assertThat(classUnderTest.getAuthorizationStatusFlow().first())
                .isEqualTo(AuthorizationStatus.AuthorizedAlways)
        }
    }

    @Test
    fun check_RequestPermission() {
        buildConfigProvider.mockSdkInt = VERSION_CODES.TIRAMISU
        testPermissionManager.askedPermissionsMockMap[Manifest.permission.POST_NOTIFICATIONS] = true
        runTest {
            assertThat(classUnderTest.requestPermission(activity).first())
                .isEqualTo(true)
        }
    }

    @Test
    fun check_getSettingsIntent() {
        val mockIntent = mockk<Intent>()
        val action = slot<String>()
        val extraKey = slot<String>()
        val extraValue = slot<String>()

        every { activity.packageName } returns "packageName"
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setAction(capture(action)) } returns mockIntent
        every {
            anyConstructed<Intent>().putExtra(
                capture(extraKey),
                capture(extraValue)
            )
        } returns mockIntent

        val intent = classUnderTest.getSettingsIntent(activity)
        assertThat(action.captured).isEqualTo(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        assertThat(extraKey.captured).isEqualTo(Settings.EXTRA_APP_PACKAGE)
        assertThat(extraValue.captured).isEqualTo("packageName")
    }
}
