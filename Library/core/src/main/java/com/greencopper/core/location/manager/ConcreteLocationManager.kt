package com.greencopper.core.location.manager

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.location.RegionsRemoteStateEntry
import com.greencopper.core.location.localstorage.location
import com.greencopper.core.location.provider.GeoLocationRequest
import com.greencopper.core.location.provider.GeolocationProvider
import com.greencopper.core.location.recipe.Region
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

internal class ConcreteLocationManager(
    private val geolocationProvider: GeolocationProvider,
    private val remoteStateDispatcher: RemoteStateDispatcher,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val scope: CoroutineScope,
) : LocationManager {
    override val monitoredRegions: Set<Region>
        get() = _monitoredRegions.toSet()
    private val _monitoredRegions = mutableSetOf<Region>()

    override suspend fun updateState(regionId: Int, entered: Boolean) {
        val region = monitoredRegions.firstOrNull {
            it.id == regionId
        } ?: return

        updateCurrentRegions(region, entered)
    }

    override suspend fun startMonitoring(region: Region) {
        if (monitoredRegions.contains(region)) {
            return
        }

        geolocationProvider.addGeofence(region) {
            _monitoredRegions.add(region)
            requestLocationUpdatesIfNeeded()
        }

        checkDistanceToLastKnownLocation(monitoredRegions)
    }

    override fun stopMonitoring(region: Region) {
        if (monitoredRegions.contains(region).not()) {
            return
        }

        geolocationProvider.removeGeofence(region) {
            _monitoredRegions.remove(region)
            updateCurrentRegions(region, false)
        }
    }

    override suspend fun resetMonitoring(regions: List<Region>) {
        with(geolocationProvider) {
            removeAllGeofences {
                if (regions.isNotEmpty()) {
                    addGeofences(regions)
                }
            }
        }
        _monitoredRegions.apply {
            clear()
            addAll(regions)
            requestLocationUpdatesIfNeeded()
        }

        checkDistanceToLastKnownLocation(monitoredRegions)
    }

    private fun requestLocationUpdatesIfNeeded() {
        if (monitoredRegions.isNotEmpty() && !geolocationProvider.isRequestingLocationUpdates()) {
            scope.launch {
                geolocationProvider.requestLocationUpdates(
                    Random.nextLong(),
                    GeoLocationRequest(GeoLocationRequest.Priority.MEDIUM)
                ).collect {
                    checkDistanceToLastKnownLocation(monitoredRegions)
                }
            }
        }
    }

    private suspend fun checkDistanceToLastKnownLocation(regions: Set<Region>) {
        val enteredRegions: Set<Int> = if (regions.isNotEmpty()) {
            geolocationProvider.getLastKnownLocation()?.let { location ->
                regions.filter { region ->
                    location.distanceTo(region.location) <= region.radiusInMeters
                }
                    .map { it.id }
                    .toSet()
            } ?: emptySet()
        } else {
            emptySet()
        }

        saveAsCurrentRegionsAndDispatch(enteredRegions)
    }

    private fun updateCurrentRegions(region: Region, entered: Boolean) {
        val enteredRegions =
            lazyLocalStorage.resolve().app.core.location.currentRegions.value.toMutableSet()

        when {
            entered -> enteredRegions.add(region.id)
            else -> enteredRegions.remove(region.id)
        }
        saveAsCurrentRegionsAndDispatch(enteredRegions)
    }

    private fun saveAsCurrentRegionsAndDispatch(regions: Set<Int>) {
        val localStorage = lazyLocalStorage.resolve()
        val currentRegions = localStorage.app.core.location.currentRegions.value
        if (regions == currentRegions) {
            return // skip if no changes found
        }
        localStorage.app.core.location.currentRegions.value = regions

        remoteStateDispatcher.dispatch(
            entry = RegionsRemoteStateEntry(regions)
        )
    }
}
