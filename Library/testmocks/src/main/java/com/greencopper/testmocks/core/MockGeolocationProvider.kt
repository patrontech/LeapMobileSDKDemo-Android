package com.greencopper.testmocks.core

import android.location.Location
import com.greencopper.core.location.provider.GeoLocationRequest
import com.greencopper.core.location.provider.GeolocationProvider
import com.greencopper.core.location.recipe.Region
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.math.*

public class MockGeolocationProvider : GeolocationProvider {

    public var addGeofenceCalled: Boolean = false
    public var addGeofencesCalled: Boolean = false
    public var removeGeofenceCalled: Boolean = false
    public var removeAllGeofencesCalled: Boolean = false
    private var _currentLocationLat: Double = 0.0
    private var _currentLocationLong: Double = 0.0
    public var currentLocation: Location? = mockk<Location>().apply {
        every { getLatitude() } answers { _currentLocationLat }
        every { getLongitude() } answers { _currentLocationLong }

        every { setLatitude(any()) } answers {
            _currentLocationLat = it.invocation.args.first() as Double
        }
        every { setLongitude(any()) } answers {
            _currentLocationLong = it.invocation.args.first() as Double
        }

        every { distanceTo(any()) } answers {
            val secondLocation = it.invocation.args.first() as Location
            (acos(
                sin(latitude) * sin(secondLocation.latitude) + cos(latitude) * cos(secondLocation.latitude) * cos(
                    secondLocation.longitude - longitude
                )
            ) * 6371).toFloat()
        }
    }

    public val callbackIds: MutableList<Long> = mutableListOf()

    public fun resetVerifiers() {
        addGeofenceCalled = false
        addGeofencesCalled = false
        removeGeofenceCalled = false
        removeAllGeofencesCalled = false
    }

    override suspend fun getLastKnownLocation(): Location? = currentLocation

    override suspend fun requestCurrentLocation(request: GeoLocationRequest): Location? = currentLocation

    override fun requestLocationUpdates(idListener: Long, request: GeoLocationRequest): Flow<Location?> {
        callbackIds.add(idListener)
        return flowOf(currentLocation)
    }

    override fun removeLocationUpdates(idListener: Long) {
        callbackIds.remove(idListener)
    }

    override fun addGeofence(region: Region, onSuccess: (() -> Unit)?) {
        addGeofenceCalled = true
        onSuccess?.invoke()
    }

    override fun addGeofences(regions: List<Region>, onSuccess: (() -> Unit)?) {
        addGeofencesCalled = true
        onSuccess?.invoke()
    }

    override fun removeGeofence(region: Region, onSuccess: (() -> Unit)?) {
        removeGeofenceCalled = true
        onSuccess?.invoke()
    }

    override fun removeAllGeofences(onSuccess: (() -> Unit)?) {
        removeAllGeofencesCalled = true
        onSuccess?.invoke()
    }

    override fun isRequestingLocationUpdates(): Boolean = callbackIds.isNotEmpty()
}
