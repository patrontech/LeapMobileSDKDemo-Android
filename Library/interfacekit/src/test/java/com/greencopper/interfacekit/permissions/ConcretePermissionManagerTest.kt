package com.greencopper.interfacekit.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.permissions.RationalePanelConfig
import com.greencopper.core.permissions.SettingsPanelConfig
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.permissions.ui.requestPermissions
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.interfacekit.MockRouteController
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConcretePermissionManagerTest : CoroutineTest() {

    init {
        Toolkit.setupTest()
        mockkStatic(ContextCompat::class)
        mockkStatic(ActivityCompat::class)
        every {
            ActivityCompat.checkSelfPermission(
                any(),
                any()
            )
        } returns PackageManager.PERMISSION_DENIED

        mockkStatic("com.greencopper.interfacekit.permissions.ui.PermissionsHeadlessFragmentKt")
    }

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private val context: Context = mockk(relaxed = true)
    private val lazyLocalStorage = LazyResolver.adhoc(LocalStorage("project"))
    private val routeController: MockRouteController = spyk(MockRouteController())
    private val permissionManager: ConcretePermissionManager = spyk(
        ConcretePermissionManager(
            context,
            routeController,
            lazyLocalStorage,
            testScope
        ), recordPrivateCalls = true
    )
    private val activity: FragmentActivity = mockk()

    private val rationaleDialogConfig = RationalePanelConfig(
        "This is my title rationale",
        "We do this for you!",
        "Got it"
    )
    private val settingsDialogConfig = SettingsPanelConfig(
        "This is title settings",
        "Do you want to open settings?",
        "Settings",
        "Not now",
        mockk()
    )

    override fun afterEach() {}

    @Test
    fun whenPermissionsNotGranted_ReturnFalse() {
        assertThat(permissionManager.hasAllPermissions(*permissions)).isFalse
    }

    @Test
    fun whenOnlyOnePermissionNotGranted_ReturnFalse() {
        //given
        every {
            ActivityCompat.checkSelfPermission(
                any(),
                permissions[0]
            )
        } returns PackageManager.PERMISSION_GRANTED

        //then
        assertThat(permissionManager.hasAllPermissions(*permissions)).isFalse
    }

    @Test
    fun whenAllPermissionsGranted_ReturnTrue() {
        //given
        every {
            ActivityCompat.checkSelfPermission(
                any(),
                permissions[0]
            )
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ActivityCompat.checkSelfPermission(
                any(),
                permissions[1]
            )
        } returns PackageManager.PERMISSION_GRANTED

        //then
        assertThat(permissionManager.hasAllPermissions(*permissions)).isTrue
    }

    @Test
    fun whenAllPermissionsGranted_flowShouldReturnTrue() {
        //given
        every {
            ActivityCompat.checkSelfPermission(
                any(),
                permissions[0]
            )
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ActivityCompat.checkSelfPermission(
                any(),
                permissions[1]
            )
        } returns PackageManager.PERMISSION_GRANTED

        //when
        runTest {
            val result = permissionManager.startPermissionsRequestFlow(
                activity,
                rationaleDialogConfig,
                settingsDialogConfig,
                *permissions
            ).first()

            //then
            verify(exactly = 0) { permissionManager["isFirstTimeAskingPermission"](any<String>()) }
            assertThat(result).isTrue
        }

    }

    @Test
    fun whenFirstTimeRequesting_shouldRequestPermission_shouldStore() {
        //given
        coEvery { activity.requestPermissions(*permissions) } returns permissions.associate {
            Pair(it, true)
        }

        //when
        runTest {
            val result = permissionManager.startPermissionsRequestFlow(
                activity,
                rationaleDialogConfig,
                settingsDialogConfig,
                *permissions
            ).first()

            //then
            coVerify(exactly = 1) {
                activity.requestPermissions(*permissions)
            }
            assertThat(lazyLocalStorage.resolve().app.interfaceKit.permissions.askedPermissions.value).isEqualTo(
                setOf(*permissions)
            )
            assertThat(result).isTrue
        }
    }

    @Test
    fun whenNotFirstTimeRequesting_withRationale_withShouldShowRationaleTrue_shouldShowRationaleDialog_shouldReturnTrueIfGranted() {
        //given
        lazyLocalStorage.resolve().app.interfaceKit.permissions.askedPermissions.value =
            setOf(*permissions)
        every { ActivityCompat.shouldShowRequestPermissionRationale(any(), any()) } returns true
        coEvery { activity.requestPermissions(*permissions) } returns permissions.associate {
            Pair(
                it,
                true
            )
        }
        routeController.willSimulateClickAlert(MockRouteController.AlertButton.POSITIVE)

        //when
        runTest {
            val result = permissionManager.startPermissionsRequestFlow(
                activity,
                rationaleDialogConfig,
                settingsDialogConfig,
                *permissions
            ).first()

            coVerify(exactly = 1) {
                routeController.showAlert(
                    title = any(),
                    message = any(),
                    positiveText = any(),
                    negativeText = any(),
                    onPositiveClicked = any(),
                    onNegativeClicked = any(),
                    onDismissed = any(),
                    isCancelable = any()
                )
            }
            assertThat(result).isTrue
        }

    }

    @Test
    fun whenNotFirstTimeRequesting_withoutRationale_withShouldShowRationaleTrue_shouldRequestPermission_shouldReturnTrueIfGranted() {
        //given
        lazyLocalStorage.resolve().app.interfaceKit.permissions.askedPermissions.value =
            setOf(*permissions)
        every { ActivityCompat.shouldShowRequestPermissionRationale(any(), any()) } returns true
        coEvery { activity.requestPermissions(*permissions) } returns permissions.associate {
            Pair(
                it,
                true
            )
        }

        //when
        runTest {
            val result =
                permissionManager.startPermissionsRequestFlow(
                    activity,
                    null,
                    settingsDialogConfig,
                    *permissions
                ).first()

            //then
            coVerify(exactly = 1) {
                activity.requestPermissions(*permissions)
            }
            assertThat(result).isTrue
        }
    }

    @Test
    fun whenNotFirstTimeRequesting_withoutRationale_withShouldShowRationaleTrue_shouldRequestPermission_shouldReturnFalseIfDenied() {
        //given
        lazyLocalStorage.resolve().app.interfaceKit.permissions.askedPermissions.value =
            setOf(*permissions)
        every { ActivityCompat.shouldShowRequestPermissionRationale(any(), any()) } returns true
        coEvery { activity.requestPermissions(*permissions) } returns permissions.associate {
            Pair(
                it,
                false
            )
        }

        //when
        runTest {
            val result =
                permissionManager.startPermissionsRequestFlow(
                    activity,
                    null,
                    settingsDialogConfig,
                    *permissions
                ).first()

            //then
            coVerify(exactly = 1) {
                activity.requestPermissions(*permissions)
            }
            assertThat(result).isFalse
        }
    }

    @Test
    fun whenNotFirstTimeRequesting_withShouldShowRationaleFalse_withSettingsDialog_shouldShowSettingsDialog_shouldReturnFalseIfDenied() {
        //given
        lazyLocalStorage.resolve().app.interfaceKit.permissions.askedPermissions.value =
            setOf(*permissions)
        every { ActivityCompat.shouldShowRequestPermissionRationale(any(), any()) } returns false
        routeController.willSimulateClickAlert(MockRouteController.AlertButton.NEGATIVE)

        //when
        runTest {
            val result = permissionManager.startPermissionsRequestFlow(
                activity,
                rationaleDialogConfig,
                settingsDialogConfig,
                *permissions
            ).first()

            //then
            coVerify(exactly = 1) {
                routeController.showAlert(
                    title = any(),
                    message = any(),
                    positiveText = any(),
                    negativeText = any(),
                    onPositiveClicked = any(),
                    onNegativeClicked = any(),
                    onDismissed = any(),
                    isCancelable = any()
                )
            }
            assertThat(result).isFalse
        }
    }

    @Test
    fun whenNotFirstTimeRequesting_withShouldShowRationaleFalse_withoutSettingsDialog_shouldDoNothing() {
        //given
        lazyLocalStorage.resolve().app.interfaceKit.permissions.askedPermissions.value =
            setOf(*permissions)
        every { ActivityCompat.shouldShowRequestPermissionRationale(any(), any()) } returns false

        //when
        runTest {
            val result = permissionManager.startPermissionsRequestFlow(
                activity,
                rationaleDialogConfig,
                null,
                *permissions
            ).first()

            //then
            assertThat(result).isFalse
        }
    }
}
