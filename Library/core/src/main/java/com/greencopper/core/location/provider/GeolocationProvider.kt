package com.greencopper.core.location.provider

import android.location.Location
import com.greencopper.core.location.recipe.Region
import kotlinx.coroutines.flow.Flow

public interface GeolocationProvider {
    public suspend fun getLastKnownLocation(): Location?
    public suspend fun requestCurrentLocation(request: GeoLocationRequest): Location?
    public fun requestLocationUpdates(idListener: Long, request: GeoLocationRequest): Flow<Location?>
    public fun removeLocationUpdates(idListener: Long)
    public fun addGeofence(region: Region, onSuccess: (() -> Unit)? = null)
    public fun addGeofences(regions: List<Region>, onSuccess: (() -> Unit)? = null)
    public fun removeGeofence(region: Region, onSuccess: (() -> Unit)? = null)
    public fun removeAllGeofences(onSuccess: (() -> Unit)? = null)
    public fun isRequestingLocationUpdates(): Boolean
}

public data class GeoLocationRequest(
    val priority: Priority,
    val suggestedRepeatMillis: Long = 0,
    val throttleRepeatMillis: Long = 0,
) {
    public enum class Priority {
        HIGH, MEDIUM, LOW, PASSIVE
    }
}
