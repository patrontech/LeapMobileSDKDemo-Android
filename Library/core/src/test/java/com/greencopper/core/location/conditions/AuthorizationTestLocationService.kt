package com.greencopper.core.location.conditions

import androidx.fragment.app.FragmentActivity
import com.greencopper.core.location.recipe.Region
import com.greencopper.core.location.service.LocationService
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.core.permissions.RationalePanelConfig
import com.greencopper.core.permissions.SettingsPanelConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class AuthorizationTestLocationService(
    private val authorizationStatus: AuthorizationStatus
) : LocationService {
    override val defaultRationalePanelConfig: RationalePanelConfig
        get() = TODO("Not yet implemented")
    override val defaultSettingsPanelConfig: SettingsPanelConfig
        get() = TODO("Not yet implemented")
    override val currentRegions: Set<Region>
        get() = TODO("Not yet implemented")

    override suspend fun initialize() {
        TODO("Not yet implemented")
    }

    override fun hasFineForegroundPermission(): Boolean = TODO("Not yet implemented")

    override fun hasOneForegroundPermission(): Boolean = TODO("Not yet implemented")

    override fun hasBackgroundAndForegroundPermission(): Boolean = TODO("Not yet implemented")

    override fun getAuthorizationStatus(): AuthorizationStatus = authorizationStatus

    override fun getAuthorizationStatusFlow(): StateFlow<AuthorizationStatus> =
        MutableStateFlow(authorizationStatus)

    override fun requestPermissions(
        activity: FragmentActivity,
        rationalePanelConfig: RationalePanelConfig?,
        settingsPanelConfig: SettingsPanelConfig?,
        needsBackgroundLocation: Boolean
    ): Flow<Boolean> {
        TODO("Not yet implemented")
    }
}