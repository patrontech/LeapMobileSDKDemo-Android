package com.greencopper.core.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.permissions.PermissionManager
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class EnableBluetoothHeadlessFragment : Fragment() {

    private val deferredCheck = CompletableDeferred<Unit>()
    private val bluetoothRequestCode = 7

    private val permissionManager: PermissionManager by lazy { App.resolve() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            lifecycleScope.launch { collectPermissions() }
        } else {
            requestEnableBluetooth()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == bluetoothRequestCode) {
            deferredCheck.complete(Unit)
        }
    }

    suspend fun awaitCheck() = deferredCheck.await()

    @RequiresApi(Build.VERSION_CODES.S)
    private suspend fun collectPermissions() {
        permissionManager.startPermissionsRequestFlow(
            activity as FragmentActivity,
            null,
            null,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        ).collectLatest { granted ->
            if (granted) {
                requestEnableBluetooth()
            } else {
                deferredCheck.complete(Unit)
            }
        }
    }

    private fun requestEnableBluetooth() {
        startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), bluetoothRequestCode)
    }

    internal fun exit() {
        activity?.run {
            if (!isFinishing) supportFragmentManager
                .beginTransaction()
                .remove(this@EnableBluetoothHeadlessFragment)
                .commitAllowingStateLoss()
        }
    }
}

internal suspend fun FragmentActivity.enableBluetooth() {
    val fragment = EnableBluetoothHeadlessFragment()

    supportFragmentManager
        .beginTransaction()
        .add(fragment, null)
        .commit()

    val awaitCheck = fragment.awaitCheck()
    fragment.exit()
    return awaitCheck
}