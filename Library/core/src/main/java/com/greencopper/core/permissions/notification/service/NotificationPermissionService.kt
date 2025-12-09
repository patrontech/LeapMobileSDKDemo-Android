package com.greencopper.core.permissions.notification.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.greencopper.core.permissions.*
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.coroutines.flow.*

public interface NotificationPermissionService {
    public fun getAuthorizationStatus(): AuthorizationStatus
    public fun getAuthorizationStatusFlow(): Flow<AuthorizationStatus>
    public fun requestPermission(
        activity: FragmentActivity,
        settingsPanelConfig: SettingsPanelConfig? = null,
    ): Flow<Boolean>

    public fun getSettingsIntent(context: Context): Intent
    public fun showSettingsDialog(settingsPanelConfig: SettingsPanelConfig): Flow<Boolean>
}

internal class ConcreteNotificationPermissionService(
    private val permissionManager: PermissionManager,
    private val buildConfigProvider: BuildConfigProvider,
) : NotificationPermissionService {

    override fun getAuthorizationStatus(): AuthorizationStatus =
        if (buildConfigProvider.sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            if (permissionManager.hasAllPermissions(NOTIFICATION_PERMISSION)) {
                AuthorizationStatus.AuthorizedAlways
            } else {
                val permissionsRequested = permissionManager.getAlreadyRequestedPermissions()
                if (permissionsRequested.contains(NOTIFICATION_PERMISSION)) {
                    AuthorizationStatus.Denied
                } else {
                    AuthorizationStatus.NotDetermined
                }
            }
        } else {
            AuthorizationStatus.AuthorizedAlways
        }

    override fun getAuthorizationStatusFlow() = PermissionManager.currentPermissions.map {
        getAuthorizationStatus()
    }

    override fun requestPermission(
        activity: FragmentActivity,
        settingsPanelConfig: SettingsPanelConfig?,
    ): Flow<Boolean> {
        return if (buildConfigProvider.sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            permissionManager.startPermissionsRequestFlow(
                activity,
                null,
                settingsPanelConfig,
                NOTIFICATION_PERMISSION
            )
        } else flowOf(true)

    }

    override fun showSettingsDialog(
        settingsPanelConfig: SettingsPanelConfig,
    ): Flow<Boolean> {
        return permissionManager.showSettingsDialog(settingsPanelConfig)
    }

    override fun getSettingsIntent(context: Context) =
        Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
    }
}
