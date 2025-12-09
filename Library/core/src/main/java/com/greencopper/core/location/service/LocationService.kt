package com.greencopper.core.location.service

import androidx.fragment.app.FragmentActivity
import com.greencopper.core.location.recipe.Region
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.core.permissions.RationalePanelConfig
import com.greencopper.core.permissions.SettingsPanelConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

public interface LocationService {

    public val defaultRationalePanelConfig: RationalePanelConfig
    public val defaultSettingsPanelConfig: SettingsPanelConfig
    public val currentRegions: Set<Region>

    public suspend fun initialize()

    public fun hasFineForegroundPermission(): Boolean

    public fun hasOneForegroundPermission(): Boolean

    public fun hasBackgroundAndForegroundPermission(): Boolean

    public fun getAuthorizationStatus(): AuthorizationStatus

    public fun getAuthorizationStatusFlow(): StateFlow<AuthorizationStatus>

    public fun requestPermissions(
        activity: FragmentActivity,
        rationalePanelConfig: RationalePanelConfig?,
        settingsPanelConfig: SettingsPanelConfig?,
        needsBackgroundLocation: Boolean
    ): Flow<Boolean>
}