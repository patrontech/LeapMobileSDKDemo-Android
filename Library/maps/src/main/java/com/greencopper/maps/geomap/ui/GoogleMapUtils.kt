package com.greencopper.maps.geomap.ui

import android.graphics.drawable.ColorDrawable
import android.location.Location
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.greencopper.interfacekit.color.Color
import com.greencopper.interfacekit.color.toColorInt
import kotlin.math.*

internal fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)

internal fun GoogleMap.moveCamera(pointCoordinates: LatLng, zoom: Float = 15f) {
    moveCamera(CameraUpdateFactory.newLatLngZoom(pointCoordinates, zoom))
}

internal fun GoogleMap.addBackgroundOverlay(backgroundColor: Color?) {
    backgroundColor ?: return
    mapType = GoogleMap.MAP_TYPE_NONE

    val overlay = ColorDrawable(backgroundColor.toColorInt())

    val options = GroundOverlayOptions()
        .image(BitmapDescriptorFactory.fromBitmap(overlay.toBitmap(1, 1)))
        .positionFromBounds(getDefaultOverlayBounds())
        .zIndex(-1f)

    addGroundOverlay(options)
}

internal fun GoogleMap.addImageOverlays(overlays: List<GroundOverlayOptions>) {
    overlays.forEach { options ->
        addGroundOverlay(options)
    }
}

internal fun GoogleMap.limitMapMovement(
    restrictedArea: LatLngBounds,
    screenWidth: Float,
    screenHeight: Float
) {
    setLatLngBoundsForCameraTarget(restrictedArea)
    setMinZoomPreference(restrictedArea.calculateZoomForBounds(screenWidth, screenHeight, 0))
}

internal fun GoogleMap.moveCameraToInitialPosition(
    screenWidth: Float,
    screenHeight: Float,
    center: LatLng,
    radius: Float,
    bearing: Float
) {
    moveCamera(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder()
                .zoom(center.calculateZoomForCenterRadius(screenWidth, screenHeight, radius, 0))
                .target(center)
                .bearing(bearing)
                .build()
        )
    )
}

/**
 * Calculate the zoom level the camera should have for a given target and radius.
 * It also takes into account the bearing that is intended to be used by the camera so that
 * extremities of the underlying calculated zone are place properly and won't go out of screen once
 * camera rotates to its planned bearing.
 */
internal fun LatLng.calculateZoomForCenterRadius(
    screenWidth: Float,
    screenHeight: Float,
    radius: Number,
    bearing: Number
): Float {
    val radiusD = radius.toDouble()
    val bearingD = bearing.toDouble()

    val regionRelatedToBearing = LatLngBounds.builder()
        .include(SphericalUtil.computeOffset(this, radiusD, bearingD))
        .include(SphericalUtil.computeOffset(this, radiusD, (bearingD + 90) % 360))
        .include(SphericalUtil.computeOffset(this, radiusD, (bearingD + 180) % 360))
        .include(SphericalUtil.computeOffset(this, radiusD, (bearingD + 270) % 360))
        .build()

    val ne = regionRelatedToBearing.northeast
    val sw = regionRelatedToBearing.southwest
    // whole world size is 256 * 2^N dp, where N zoom level
    // so required to calculate whole world size in dp where our region should be shown on the screen that is screenHeight dp
    // from other side we can calculate whole world size in degrees
    val regionToShowSizeHeight = abs(ne.latitude - sw.latitude)
    val wholeWorldSizeDpHeight = (MAX_LAT - MIN_LAT) / regionToShowSizeHeight * screenHeight
    val maxZoomPreferenceHeight = log2(wholeWorldSizeDpHeight / 256).toFloat()

    // same calculations for width
    val regionToShowSizeWidth = abs(ne.longitude - sw.longitude)
    val wholeWorldSizeDpWidth = (MAX_LON - MIN_LON) / regionToShowSizeWidth * screenWidth
    val maxZoomPreferenceWidth = log2(wholeWorldSizeDpWidth / 256).toFloat()

    // required to use minimum because larger distance on the screen will be shown when whole world less dp-s
    return min(maxZoomPreferenceHeight, maxZoomPreferenceWidth)
}

/** Calculate min zoom for region that should be shown */
internal fun LatLngBounds.calculateZoomForBounds(screenWidth: Float, screenHeight: Float, bearing: Number): Float =
    center.calculateZoomForCenterRadius(
        screenWidth,
        screenHeight,
        SphericalUtil.computeDistanceBetween(southwest, northeast) / 2,
        bearing,
    )
