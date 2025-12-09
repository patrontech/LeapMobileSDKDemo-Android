package com.greencopper.testmocks.core

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.core.permissions.SettingsPanelConfig
import com.greencopper.core.permissions.notification.service.NotificationPermissionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockNotificationPermissionService(
    public var authorizationStatusMock: AuthorizationStatus = AuthorizationStatus.AuthorizedAlways,
    public var requestPermissionCalled: Boolean = false,
    public var getSettingsIntentCalled: Boolean = false,
    public var resultFromSettings: Boolean = false,
    public var mockIntent: Intent? = null,
): NotificationPermissionService {
    override fun getAuthorizationStatus(): AuthorizationStatus =
        authorizationStatusMock

    override fun getAuthorizationStatusFlow(): Flow<AuthorizationStatus> =
        flowOf(authorizationStatusMock)

    override fun requestPermission(
        activity: FragmentActivity,
        settingsPanelConfig: SettingsPanelConfig?,
    ): Flow<Boolean> {
        requestPermissionCalled = true
        return flowOf(true)
    }

    override fun getSettingsIntent(context: Context): Intent {
        getSettingsIntentCalled = true
        return mockIntent ?: Intent()
    }

    override fun showSettingsDialog(settingsPanelConfig: SettingsPanelConfig): Flow<Boolean> =
        flowOf(resultFromSettings)
}
