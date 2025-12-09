package com.greencopper.testmocks.core

import androidx.fragment.app.FragmentActivity
import com.greencopper.core.location.recipe.Region
import com.greencopper.core.location.service.LocationService
import com.greencopper.core.permissions.*
import io.mockk.mockk
import kotlinx.coroutines.flow.*

public class MockLocationService : LocationService {

    public var foregroundPermission: Boolean = true
    public var backgroundPermission: Boolean = true
    public var currentAuthStatus: AuthorizationStatus = AuthorizationStatus.AuthorizedAlways

    override val defaultRationalePanelConfig: RationalePanelConfig = RationalePanelConfig("", "", "")
    override val defaultSettingsPanelConfig: SettingsPanelConfig = SettingsPanelConfig("", "", "", "", mockk())
    override val currentRegions: Set<Region> = setOf()

    override suspend fun initialize() {}

    override fun hasFineForegroundPermission(): Boolean = foregroundPermission

    override fun hasOneForegroundPermission(): Boolean = foregroundPermission

    override fun hasBackgroundAndForegroundPermission(): Boolean = foregroundPermission && backgroundPermission

    override fun getAuthorizationStatus(): AuthorizationStatus = currentAuthStatus

    override fun getAuthorizationStatusFlow(): StateFlow<AuthorizationStatus> = MutableStateFlow(currentAuthStatus)

    override fun requestPermissions(
        activity: FragmentActivity,
        rationalePanelConfig: RationalePanelConfig?,
        settingsPanelConfig: SettingsPanelConfig?,
        needsBackgroundLocation: Boolean
    ): Flow<Boolean> = flowOf(foregroundPermission)
}
