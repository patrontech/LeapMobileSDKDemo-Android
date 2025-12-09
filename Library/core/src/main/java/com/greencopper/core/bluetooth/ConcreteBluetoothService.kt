package com.greencopper.core.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.greencopper.core.permissions.PermissionManager
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.w
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class ConcreteBluetoothService(
    context: Context,
    buildConfigProvider: BuildConfigProvider,
    scope: CoroutineScope,
) : BluetoothService {

    private var isBluetoothOn = MutableStateFlow(false)
    private val _isPermissionGranted = MutableStateFlow(false)
    override val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted

    override val isBluetoothEnabled: Flow<Boolean> = combine(
        isBluetoothOn,
        isPermissionGranted
    ) { bluetoothEnabled, permissionGranted ->
        bluetoothEnabled && permissionGranted
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
            isBluetoothOn.value = state == BluetoothAdapter.STATE_ON
        }
    }

    init {
        try {
            val manager = context.getSystemService(BluetoothManager::class.java)
            val adapter: BluetoothAdapter? = manager.adapter

            isBluetoothOn.value = adapter?.isEnabled ?: false

            context.registerReceiver(
                broadcastReceiver,
                IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
                Context.RECEIVER_NOT_EXPORTED,
            )

            scope.launch {
                PermissionManager.currentPermissions.collect { permissions ->
                    _isPermissionGranted.value = buildConfigProvider.sdkInt < Build.VERSION_CODES.S ||
                            (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT) && permissions.contains(
                                Manifest.permission.BLUETOOTH_SCAN
                            ))
                }
            }
        } catch (e: SecurityException) {
            // This is an expected exception if no modules with bluetooth permissions
            // are included in the build
            App.log.w("Trying to access BluetoothService without permission", throwable = e)
        }
    }

    override suspend fun openBluetoothSettings(activity: FragmentActivity) {
        activity.enableBluetooth()
    }
}
