package com.greencopper.core.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.greencopper.core.permissions.PermissionManager
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockPermissionManager
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.Toolkit
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConcreteBluetoothServiceTest: CoroutineTest(UnconfinedTestDispatcher())  {

    private val permissionManager: PermissionManager = MockPermissionManager()
    private val context: Context = mockk(relaxed = true)
    private val bluetoothManager: BluetoothManager = mockk()
    private val bluetoothAdapter: BluetoothAdapter = mockk()
    private val buildConfigProvider = MockBuildConfigProvider()

    init {
        Toolkit.setupTest()
        every { context.getSystemService(BluetoothManager::class.java) } returns bluetoothManager
        every { bluetoothManager.adapter } returns bluetoothAdapter
        mockkStatic("com.greencopper.core.bluetooth.EnableBluetoothHeadlessFragmentKt")
    }

    override fun afterEach() {}

    @Test
    fun permissionNotGrantedBluetoothOff_bluetoothEnabled_isFalse() {
        every { bluetoothAdapter.isEnabled } returns false
        permissionManager.setCurrentPermissions(setOf())
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.S

        val bluetoothService = ConcreteBluetoothService(context, buildConfigProvider, testScope)
        runTest {
            assertThat(bluetoothService.isBluetoothEnabled.first()).isFalse
        }
    }

    @Test
    fun permissionNotGrantedBluetoothOn_bluetoothEnabled_isFalse() {
        every { bluetoothAdapter.isEnabled } returns true
        permissionManager.setCurrentPermissions(setOf())
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.S

        val bluetoothService = ConcreteBluetoothService(context, buildConfigProvider, testScope)
        runTest {
            assertThat(bluetoothService.isBluetoothEnabled.first()).isFalse
        }
    }

    @Test
    fun permissionGrantedBecauseVersionLessThenS_BluetoothOn_bluetoothEnabled_isTrue() {
        every { bluetoothAdapter.isEnabled } returns true
        permissionManager.setCurrentPermissions(setOf())
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.S - 1

        val bluetoothService = ConcreteBluetoothService(context, buildConfigProvider, testScope)
        runTest {
            assertThat(bluetoothService.isBluetoothEnabled.first()).isTrue
        }
    }

    @Test
    fun permissionGrantedBluetoothOff_bluetoothEnabled_isFalse() {
        every { bluetoothAdapter.isEnabled } returns false
        permissionManager.setCurrentPermissions(
            setOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        )
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.S

        val bluetoothService = ConcreteBluetoothService(context, buildConfigProvider, testScope)
        runTest {
            assertThat(bluetoothService.isBluetoothEnabled.first()).isFalse
        }
    }

    @Test
    fun permissionGrantedBluetoothOn_bluetoothEnabled_isTrue() {
        every { bluetoothAdapter.isEnabled } returns true
        permissionManager.setCurrentPermissions(
            setOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        )
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.S

        val bluetoothService = ConcreteBluetoothService(context, buildConfigProvider, testScope)
        runTest {
            bluetoothService.isBluetoothEnabled.first()
        }
    }

    @Test
    fun openBluetoothSettings_callsEnableBluetooth() {
        every { bluetoothAdapter.isEnabled } returns true
        val bluetoothService = ConcreteBluetoothService(context, buildConfigProvider, testScope)

        val activity: FragmentActivity = mockk()
        coEvery { activity.enableBluetooth() } returns Unit

        runTest {
            bluetoothService.openBluetoothSettings(activity)
        }

        coVerify { activity.enableBluetooth() }
    }

    @Test
    fun securityException_doesntCrash() {
        every { context.getSystemService(BluetoothManager::class.java) } throws SecurityException()
        every { bluetoothManager.adapter } throws SecurityException()
        val bluetoothService = ConcreteBluetoothService(context, buildConfigProvider, testScope)
        assertThat(bluetoothService).isNotNull
    }
}
