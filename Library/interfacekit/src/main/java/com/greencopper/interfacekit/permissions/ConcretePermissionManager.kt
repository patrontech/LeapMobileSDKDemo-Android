package com.greencopper.interfacekit.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.permissions.*
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.permissions.ui.requestPermissions
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class ConcretePermissionManager(
    private val context: Context,
    private val routeController: RouteController,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val scope: CoroutineScope,
) : PermissionManager {

    private var settingsCallback: ActivityResultLauncher<PermissionsContract.PermissionsContractData>? =
        null
    private var channelSettingsCallback: Channel<Boolean> = Channel()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun registerSettingsCallback(lifecycleOwner: LifecycleOwner) {
        settingsCallback = (lifecycleOwner as? ComponentActivity)?.registerForActivityResult(
            PermissionsContract()
        ) {
            channelSettingsCallback.trySend(hasAllPermissions(*it.toTypedArray()))
        }
    }

    override fun getAlreadyRequestedPermissions(): Set<String> =
        lazyLocalStorage.resolve().app.interfaceKit.permissions.askedPermissions.value

    override fun hasAllPermissions(vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }

    override fun refreshPermissionsStatus(appManifestPermissions: List<String>) {
        val grantedPermissions = appManifestPermissions.filter {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        setCurrentPermissions(grantedPermissions.toSet())
    }

    override fun shouldShowRationaleOfPermission(activity: Activity, permission: String): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    override fun startPermissionsRequestFlow(
        activity: FragmentActivity,
        rationalePanelConfig: RationalePanelConfig?,
        settingsPanelConfig: SettingsPanelConfig?,
        vararg permissions: String,
    ): Flow<Boolean> = callbackFlow {

        if (permissions.all { hasAllPermissions(it) }) {
            trySend(true)
            awaitClose()
            return@callbackFlow
        }

        val isFirstTime = permissions.any { isFirstTimeAskingPermission(it) }
        trySend(
            when {
                isFirstTime -> {
                    requestPermissions(activity, *permissions).first().all { it.value }
                }

                permissions.any {
                    shouldShowRationaleOfPermission(activity, it)
                } -> {
                    rationalePanelConfig?.let {
                        showRationaleDialog(
                            activity,
                            it,
                            *permissions
                        ).first()
                    } ?: requestPermissions(activity, *permissions).first().all { it.value }
                }

                else -> {
                    settingsPanelConfig?.let {
                        showSettingsDialog(
                            settingsPanelConfig,
                            *permissions
                        ).first()
                    } ?: false
                }
            })

        awaitClose()
    }

    override fun showSettingsDialog(
        settingsPanelConfig: SettingsPanelConfig,
        vararg permissions: String,
    ): Flow<Boolean> = callbackFlow {

        routeController.showAlert(
            title = settingsPanelConfig.title,
            message = settingsPanelConfig.message ?: "",
            positiveText = settingsPanelConfig.positiveButtonString,
            negativeText = settingsPanelConfig.negativeButtonString,
            onPositiveClicked = {
                val intent = settingsPanelConfig.intentToOpen
                intent.resolveActivity(context.packageManager)?.let {
                    settingsCallback?.launch(
                        PermissionsContract.PermissionsContractData(
                            intent,
                            permissions.toSet()
                        )
                    )
                    scope.launch {
                        trySend(channelSettingsCallback.receive())
                    }
                }
            },
            onNegativeClicked = {
                trySend(false)
            },
            onDismissed = null,
            isCancelable = false
        )

        awaitClose()
    }

    private suspend fun showRationaleDialog(
        activity: FragmentActivity,
        rationalePanelConfig: RationalePanelConfig,
        vararg permissions: String,
    ): Flow<Boolean> = callbackFlow {

        routeController.showAlert(
            title = rationalePanelConfig.title,
            message = rationalePanelConfig.message ?: "",
            positiveText = rationalePanelConfig.positiveButtonString,
            negativeText = null,
            onPositiveClicked = {
                scope.launch {
                    trySend(requestPermissions(activity, *permissions).first().all { it.value })
                }
            },
            onNegativeClicked = null,
            onDismissed = null,
            isCancelable = false
        )

        awaitClose()
    }

    override suspend fun requestPermissions(
        activity: FragmentActivity,
        vararg permissions: String,
    ): Flow<Map<String, Boolean>> = callbackFlow {
        val result = activity.requestPermissions(*permissions)

        val localStorage = lazyLocalStorage.resolve()
        val permissionsAsked =
            localStorage.app.interfaceKit.permissions.askedPermissions.value.toMutableSet()
        permissionsAsked.addAll(permissions)
        localStorage.app.interfaceKit.permissions.askedPermissions.value = permissionsAsked

        trySend(result)
        awaitClose()
    }

    private fun isFirstTimeAskingPermission(permission: String): Boolean {
        val permissionsAsked =
            lazyLocalStorage.resolve().app.interfaceKit.permissions.askedPermissions.value
        return !permissionsAsked.contains(permission)
    }
}
