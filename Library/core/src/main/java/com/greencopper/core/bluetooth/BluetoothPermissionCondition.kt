package com.greencopper.core.bluetooth

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.conditionchecker.UnparameterizedCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public class BluetoothPermissionCondition(
    private val bluetoothService: BluetoothService
) : UnparameterizedCondition() {

    override fun check(): Boolean = !bluetoothService.isPermissionGranted.value

    override fun checkFlow(): Flow<Boolean> = bluetoothService.isPermissionGranted.map { !it }

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("Core.Bluetooth.Permissions", 1)
    }
}
