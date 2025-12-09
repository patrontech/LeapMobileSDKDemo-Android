package com.greencopper.interfacekit.permissions.ui

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CompletableDeferred

internal class PermissionsHeadlessFragment : Fragment() {
    private val deferredGrant = CompletableDeferred<Map<String, Boolean>>()
    private val permissions: Array<String> by lazy {
        arguments?.getStringArray(PERMISSIONS_ARGS_TAG) ?: emptyArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        if (permissions.isEmpty()) {
            complete(mapOf("" to true))
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST_TAG)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_TAG -> {
                complete(
                    permissions.zip(
                        grantResults.asIterable().map {
                            it == PackageManager.PERMISSION_GRANTED
                        }
                    ).toMap()
                )
            }
        }
    }

    internal fun exit() {
        retainInstance = false
        activity?.run {
            if (!isFinishing) supportFragmentManager
                .beginTransaction()
                .remove(this@PermissionsHeadlessFragment)
                .commitAllowingStateLoss()
        }
    }

    suspend fun awaitGrant(): Map<String, Boolean> = deferredGrant.await()

    private fun complete(mapResult: Map<String, Boolean>) {
        deferredGrant.complete(mapResult)
        exit()
    }

    companion object {
        const val PERMISSION_REQUEST_TAG = 1
        const val PERMISSIONS_ARGS_TAG = "permissions"
    }
}

internal suspend fun FragmentActivity.requestPermissions(vararg permissions: String): Map<String, Boolean> {
    if (permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
        return permissions.associateWith { true }
    }

    val args = Bundle().apply {
        putStringArray(
            PermissionsHeadlessFragment.PERMISSIONS_ARGS_TAG,
            permissions
        )
    }
    val permissionsFragment = PermissionsHeadlessFragment().apply { arguments = args }
    supportFragmentManager
        .beginTransaction()
        .add(permissionsFragment, null)
        .commit()

    val awaitGrant = permissionsFragment.awaitGrant()
    permissionsFragment.exit()
    return awaitGrant
}
