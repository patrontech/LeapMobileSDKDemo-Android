package com.greencopper.core.permissions

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import kotlinx.coroutines.flow.*

public interface PermissionManager : LifecycleObserver {
    public fun hasAllPermissions(vararg permissions: String): Boolean
    public fun shouldShowRationaleOfPermission(activity: Activity, permission: String): Boolean
    public fun getAlreadyRequestedPermissions(): Set<String>

    /**
     * @return A representation of each permissions and if they are
     * now granted or not.
     */
    public suspend fun requestPermissions(
        activity: FragmentActivity,
        vararg permissions: String
    ): Flow<Map<String, Boolean>>

    /**
     * @return Whether all given permissions were granted or not
     */
    public fun startPermissionsRequestFlow(
        activity: FragmentActivity,
        rationalePanelConfig: RationalePanelConfig?,
        settingsPanelConfig: SettingsPanelConfig?,
        vararg permissions: String,
    ): Flow<Boolean>

    public fun refreshPermissionsStatus(appManifestPermissions: List<String>)

    public fun setCurrentPermissions(permissions: Set<String>) {
        _currentPermissions.value = permissions
    }

    public fun showSettingsDialog(settingsPanelConfig: SettingsPanelConfig, vararg permissions: String): Flow<Boolean>

    public companion object {
        private val _currentPermissions = MutableStateFlow(emptySet<String>())
        public val currentPermissions: StateFlow<Set<String>> =
            _currentPermissions.asStateFlow()
    }
}

public data class RationalePanelConfig(
    val title: String,
    val message: String?,
    val positiveButtonString: String?
)

public data class SettingsPanelConfig(
    val title: String,
    val message: String?,
    val positiveButtonString: String?,
    val negativeButtonString: String?,
    val intentToOpen: Intent
)
