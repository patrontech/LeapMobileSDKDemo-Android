package com.greencopper.testmocks.core

import androidx.fragment.app.FragmentActivity
import com.greencopper.core.bluetooth.BluetoothService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class MockBluetoothService : BluetoothService {

    private val _isPermissionGranted = MutableStateFlow(false)
    override val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted

    private val _isBluetoothEnabled = MutableStateFlow(false)
    override val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled

    public fun setBluetoothEnabled(enabled: Boolean) {
        _isBluetoothEnabled.value = enabled
    }

    public fun setPermissionGranted(permissionGranted: Boolean) {
        _isPermissionGranted.value = permissionGranted
    }

    public override suspend fun openBluetoothSettings(activity: FragmentActivity): Unit = Unit
}
