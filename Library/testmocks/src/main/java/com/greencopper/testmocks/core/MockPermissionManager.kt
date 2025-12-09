package com.greencopper.testmocks.core

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.greencopper.core.permissions.*
import kotlinx.coroutines.flow.*

public class MockPermissionManager(
    public var alreadyRequestedPermission: Set<String> = emptySet(),
    public var shouldShowRationaleMockMap: MutableMap<String, Boolean> = mutableMapOf(),
    public var alreadyGrantedPermissionsMockMap: MutableMap<String, Boolean> = mutableMapOf(),
    public var askedPermissionsMockMap: MutableMap<String, Boolean> = mutableMapOf(),
    public var resultFromSettings: Boolean = false,
) : PermissionManager {

    public var requestedPermissions: List<String> = emptyList()

    override fun hasAllPermissions(vararg permissions: String): Boolean {
        return permissions.filter {
            alreadyGrantedPermissionsMockMap[it] == true
        }.size == permissions.size
    }

    override fun shouldShowRationaleOfPermission(activity: Activity, permission: String): Boolean {
        return shouldShowRationaleMockMap.getOrDefault(permission, false)
    }

    override fun getAlreadyRequestedPermissions(): Set<String> {
        return alreadyRequestedPermission
    }

    override suspend fun requestPermissions(
        activity: FragmentActivity,
        vararg permissions: String
    ): Flow<Map<String, Boolean>> = flow {
        emit(
            permissions.associateWith {
                askedPermissionsMockMap.getOrDefault(it, false)
            }
        )
    }

    override fun startPermissionsRequestFlow(
        activity: FragmentActivity,
        rationalePanelConfig: RationalePanelConfig?,
        settingsPanelConfig: SettingsPanelConfig?,
        vararg permissions: String,
    ): Flow<Boolean> {
        requestedPermissions = permissions.toList()
        return flow {
            emit(requestPermissions(activity, *permissions).first().all { it.value })
        }
    }

    override fun refreshPermissionsStatus(appManifestPermissions: List<String>) {
    }

    override fun showSettingsDialog(
        settingsPanelConfig: SettingsPanelConfig,
        vararg permissions: String,
    ): Flow<Boolean> = flowOf(resultFromSettings)

}
