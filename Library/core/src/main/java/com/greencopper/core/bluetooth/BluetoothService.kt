package com.greencopper.core.bluetooth

import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

public interface BluetoothService {

    public val isBluetoothEnabled: Flow<Boolean>
    public val isPermissionGranted: StateFlow<Boolean>

    public suspend fun openBluetoothSettings(activity: FragmentActivity)
}
