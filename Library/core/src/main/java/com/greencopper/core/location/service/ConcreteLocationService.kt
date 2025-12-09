package com.greencopper.core.location.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.location.LocationConfigurationHolder
import com.greencopper.core.location.localstorage.location
import com.greencopper.core.location.manager.LocationManager
import com.greencopper.core.location.recipe.LocationConfiguration
import com.greencopper.core.location.recipe.Region
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.core.permissions.PermissionManager
import com.greencopper.core.permissions.RationalePanelConfig
import com.greencopper.core.permissions.SettingsPanelConfig
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class ConcreteLocationService(
    private val context: Context,
    private val permissionManager: PermissionManager,
    private val locationManager: LocationManager,
    private val locationConfigurationHolder: LocationConfigurationHolder,
    private val currentProjectTagProvider: CurrentProjectTagProvider,
    private val versionProvider: BuildConfigProvider,
    private val localizationService: LocalizationService,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val scope: CoroutineScope,
) : LocationService {

    private val authorizationStatusFlow = MutableStateFlow(getAuthorizationStatus())
    private var activeProject = currentProjectTagProvider.currentProject

    private val setupMonitoringFlow: MutableStateFlow<MonitoringConfig> =
        MutableStateFlow(MonitoringConfig(null, null))

    override val defaultRationalePanelConfig by lazy {
        with(localizationService) {
            RationalePanelConfig(
                getString("location.rationale_dialog.default_title"),
                getString("location.rationale_dialog.default_message"),
                getString("permissions.rationale_dialog.positive_button")
            )
        }
    }

    override val defaultSettingsPanelConfig by lazy {
        with(localizationService) {
            SettingsPanelConfig(
                getString("location.settings_dialog.default_title"),
                getString("location.settings_dialog.default_message"),
                getString("permissions.settings_dialog.positive_button"),
                getString("permissions.settings_dialog.negative_button"),
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                }
            )
        }
    }

    override val currentRegions: Set<Region>
        get() {
            val current = lazyLocalStorage.resolve().app.core.location.currentRegions.value
            return locationConfigRegions.filter {
                current.contains(it.id)
            }.toSet()
        }

    private var locationConfigRegions = listOf<Region>()

    override suspend fun initialize() {
        scope.launch {
            locationConfigurationHolder.currentConfiguration.collect { config ->
                setupMonitoringFlow.emit(
                    MonitoringConfig(
                        config,
                        currentProjectTagProvider.currentProject
                    )
                )
            }
        }

        scope.launch {
            PermissionManager.currentPermissions.collect {
                setupMonitoringFlow.emit(
                    MonitoringConfig(
                        locationConfigurationHolder.currentConfiguration.value,
                        currentProjectTagProvider.currentProject
                    )
                )
            }
        }

        scope.launch {
            currentProjectTagProvider.currentProjectFlow.collect { project ->
                setupMonitoringFlow.emit(
                    MonitoringConfig(
                        locationConfigurationHolder.currentConfiguration.value,
                        project
                    )
                )
            }
        }

        setupMonitoringFlow.collectLatest { setupLocationMonitoring(it) }
    }

    private suspend fun setupLocationMonitoring(monitoringConfig: MonitoringConfig) {
        if (activeProject != monitoringConfig.project) {
            locationManager.resetMonitoring(emptyList())
            activeProject = monitoringConfig.project
        }

        authorizationStatusFlow.value = getAuthorizationStatus()
        locationConfigRegions = when {
            hasFineForegroundPermission() -> monitoringConfig.locationConfig?.regions
            else -> null
        } ?: emptyList()

        locationManager.resetMonitoring(locationConfigRegions)
    }

    override fun hasFineForegroundPermission(): Boolean =
        permissionManager.hasAllPermissions(*getRequiredPermissions(false).toTypedArray())

    override fun hasOneForegroundPermission(): Boolean =
        permissionManager.hasAllPermissions(Manifest.permission.ACCESS_FINE_LOCATION) ||
                permissionManager.hasAllPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)

    override fun hasBackgroundAndForegroundPermission(): Boolean =
        permissionManager.hasAllPermissions(*getRequiredPermissions(true).toTypedArray())

    override fun getAuthorizationStatus(): AuthorizationStatus =
        when {
            hasBackgroundAndForegroundPermission() -> {
                AuthorizationStatus.AuthorizedAlways
            }

            hasFineForegroundPermission() -> {
                AuthorizationStatus.AuthorizedWhenInUse
            }

            else -> {
                val permissionsRequested = permissionManager.getAlreadyRequestedPermissions()
                if (permissionsRequested.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AuthorizationStatus.Denied
                } else {
                    AuthorizationStatus.NotDetermined
                }
            }
        }

    override fun getAuthorizationStatusFlow(): StateFlow<AuthorizationStatus> =
        authorizationStatusFlow.asStateFlow()

    @SuppressLint("NewApi")
    override fun requestPermissions(
        activity: FragmentActivity,
        rationalePanelConfig: RationalePanelConfig?,
        settingsPanelConfig: SettingsPanelConfig?,
        needsBackgroundLocation: Boolean,
    ): Flow<Boolean> {

        return if (versionProvider.sdkInt >= Build.VERSION_CODES.R) {
            requestPermissionsModern(
                activity,
                rationalePanelConfig,
                settingsPanelConfig,
                needsBackgroundLocation
            )
        } else {
            requestPermissionsLegacy(
                activity,
                rationalePanelConfig,
                settingsPanelConfig,
                needsBackgroundLocation
            )
        }

    }

    private fun requestPermissionsLegacy(
        activity: FragmentActivity,
        rationalePanelConfig: RationalePanelConfig?,
        settingsPanelConfig: SettingsPanelConfig?,
        needsBackgroundLocation: Boolean,
    ): Flow<Boolean> = permissionManager.startPermissionsRequestFlow(
        activity = activity,
        rationalePanelConfig = rationalePanelConfig,
        settingsPanelConfig = settingsPanelConfig,
        permissions = getRequiredPermissions(needsBackgroundLocation).toTypedArray()
    )

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestPermissionsModern(
        activity: FragmentActivity,
        rationalePanelConfig: RationalePanelConfig?,
        settingsPanelConfig: SettingsPanelConfig?,
        needsBackgroundLocation: Boolean,
    ): Flow<Boolean> = flow {

        val foregroundPermissionAllowed = permissionManager.startPermissionsRequestFlow(
            activity = activity,
            rationalePanelConfig = rationalePanelConfig,
            settingsPanelConfig = settingsPanelConfig,
            permissions = getRequiredPermissions(false).toTypedArray()
        ).first()

        val backgroundPermissionAllowed =
            if (foregroundPermissionAllowed && needsBackgroundLocation) {
                permissionManager.startPermissionsRequestFlow(
                    activity = activity,
                    rationalePanelConfig = rationalePanelConfig,
                    settingsPanelConfig = settingsPanelConfig,
                    permissions = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                ).first()
            } else {
                true
            }

        emit(foregroundPermissionAllowed && backgroundPermissionAllowed)
    }

    private fun getRequiredPermissions(needsBackgroundLocation: Boolean): Set<String> {
        val result = mutableSetOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (needsBackgroundLocation) {
            result.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return result
    }
}

private class MonitoringConfig(
    val locationConfig: LocationConfiguration?,
    val project: String?,
)
