package com.greencopper.core.bluetooth

import com.greencopper.testmocks.core.MockBluetoothService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BluetoothPermissionConditionTest {

    private val bluetoothService = MockBluetoothService()
    private val condition = BluetoothPermissionCondition(bluetoothService)

    @Test
    fun bluetoothDisabled_check_returnsTrue() {
        bluetoothService.setPermissionGranted(false)
        assertThat(condition.check()).isTrue
    }

    @Test
    fun bluetoothEnabled_check_returnsFalse() {
        bluetoothService.setPermissionGranted(true)
        assertThat(condition.check()).isFalse
    }

    @Test
    fun bluetoothDisabled_checkFlow_returnsTrue() {
        bluetoothService.setPermissionGranted(false)
        runTest {
            assertThat(condition.checkFlow().first()).isTrue
        }
    }

    @Test
    fun bluetoothEnabled_checkFlow_returnsFalse() {
        bluetoothService.setPermissionGranted(true)
        runTest {
            assertThat(condition.checkFlow().first()).isFalse
        }
    }
}
