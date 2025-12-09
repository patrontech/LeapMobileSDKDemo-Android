package com.greencopper.maps.geomap

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.greencopper.core.data.getKiboSerializable
import com.greencopper.core.data.putKiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.location.provider.GeoLocationRequest
import com.greencopper.core.location.provider.GeolocationProvider
import com.greencopper.core.location.service.LocationService
import com.greencopper.core.permissions.*
import com.greencopper.interfacekit.color.toColorInt
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.imageservice.ImageService
import com.greencopper.interfacekit.imageservice.rotate
import com.greencopper.interfacekit.navigation.feature.FeatureResolver
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.kiba_maps.R
import com.greencopper.maps.colors.MapsColor
import com.greencopper.maps.geomap.data.MapData
import com.greencopper.maps.geomap.data.MapData.FeatureType
import com.greencopper.maps.geomap.ui.*
import com.greencopper.maps.geomap.ui.MapItem.MapFeatureItem
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.round
import kotlin.random.Random

internal class GeoMapViewModel(
    private val geolocationProvider: GeolocationProvider,
    private val locationService: LocationService,
    private val featureResolver: FeatureResolver,
    private val filterHandler: FilteringHandler,
    private val imageService: ImageService,
    private val localizationService: LocalizationService,
) : ViewModel() {

    private lateinit var data: GeoMapLayoutData

    private var savedFiltering: FilteringInfo? = null
    private val mapZoom = MutableStateFlow(0f)
    private val requestLocationCallbackId by lazy { Random.nextLong() }
    private val pinCache = mutableMapOf<ImageProperties, Bitmap>()

    internal val markerHolder = GeoMapMarkersHolder()
    internal var preselectedFeatureShown = false
    internal var lastMapViewState: Bundle? = null
    internal var savedSelectedFeatureId: String? = null

    internal val selectedRoute = MutableStateFlow<Route?>(null)
    internal val projection = MutableStateFlow<Projection?>(null)
    internal var minZoomLevel: Float = 0f
    internal var maxZoomLevel: Float = Float.MAX_VALUE

    suspend fun getLastKnownLocation(): Location? = geolocationProvider.getLastKnownLocation()
    fun requestLocationUpdates(): Flow<Location?> = geolocationProvider.requestLocationUpdates(
        requestLocationCallbackId, GeoLocationRequest(
            priority = GeoLocationRequest.Priority.HIGH,
            suggestedRepeatMillis = TimeUnit.MINUTES.toMillis(1),
            throttleRepeatMillis = TimeUnit.SECONDS.toSeconds(10)
        )
    )

    fun removeLocationUpdates() = geolocationProvider.removeLocationUpdates(requestLocationCallbackId)

    fun hasPermissionsGranted(): Boolean {
        return locationService.hasOneForegroundPermission()
    }

    fun getAuthorizationStatus(): AuthorizationStatus {
        return locationService.getAuthorizationStatus()
    }

    /**
     * @return True if permissions are granted
     */
    fun requestPermissionIfNecessary(
        activity: FragmentActivity,
    ): Flow<Boolean> {
        val rationalePanelConfig = RationalePanelConfig(
            title = activity.resources.getString(
                R.string.maps__geoMap__location_request__title,
                activity.applicationInfo.loadLabel(activity.packageManager)
                    .toString()
            ),
            message = activity.resources.getString(R.string.maps__geoMap__location_request__message),
            positiveButtonString = localizationService.getString("common.ok")
        )

        val settingsPanelConfig = SettingsPanelConfig(
            title = localizationService.getString("maps.geoMap.location_denied.title"),
            message = localizationService.getString("maps.geoMap.location_denied.message"),
            positiveButtonString = localizationService.getString("common.settings"),
            negativeButtonString = localizationService.getString("common.cancel"),
            intentToOpen = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${activity.packageName}")
            })

        return locationService.requestPermissions(
            activity = activity,
            rationalePanelConfig = rationalePanelConfig,
            settingsPanelConfig = settingsPanelConfig,
            needsBackgroundLocation = false
        )
    }

    fun getFragmentLayout(feature: FeatureInfo): Layout? {
        return try {
            featureResolver.resolve(feature)
        } catch (throwable: Throwable) {
            App.log.e("Couldn't resolve feature $feature", throwable = throwable)
            null
        }
    }

    internal fun getPreselectedLocation(): MapData.Feature? {
        val id = getPreselectionId()
        preselectedFeatureShown = true

        if (id == null) return null

        return data.geoJson.features
            .firstOrNull { it.properties.locationId == id }
    }

    private fun getPreselectionId(): String? {
        return if (data.preSelectedLocationId != null && !preselectedFeatureShown) {
            data.preSelectedLocationId
        } else if (markerHolder.selectedFeature.value?.properties?.locationId != null) {
            markerHolder.selectedFeature.value?.properties?.locationId
        } else savedSelectedFeatureId
    }

    fun getFilteringBarData(layout: Layout, screenName: String): FilteringBarData? {
        return filterHandler.buildBarData(layout, screenName)
            .takeIf { it.filters.isNotEmpty() } // Temporary - Until favorites are implemented here
    }

    fun getCurrentFilterState(): FilteringInfo? = filterHandler.currentStateToInfo

    fun setMapZoomLevel(zoom: Float) {
        mapZoom.value = zoom
    }

    suspend fun getImageOverlays(): List<GroundOverlayOptions> =
        data.geoJson.features
            .filter { it.geometry.type == FeatureType.Polygon }
            .mapNotNull { feature ->
                val image = imageService.getImageDrawable(
                    feature.properties.imageName,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                ).first().drawable as? BitmapDrawable ?: return@mapNotNull null

                val coordinates = feature.geometry.getCoordinatesList()
                // can't position overlay without at least 2 coordinates
                if (coordinates.size < 2) return@mapNotNull null
                // image overlay position is defined by the order of the coordinates:
                // bottom left, top left, top right, bottom right, bottom left
                // therefore the angle between the first 2 coordinates can define the rotation of the image
                val angle = SphericalUtil.computeHeading(coordinates[0], coordinates[1])
                val rotatedBitmap = if (abs(angle) > 0) image.bitmap.rotate(angle.toFloat()) else image.bitmap

                var bounds = LatLngBounds.builder()
                coordinates.forEach {
                    bounds = bounds.include(it)
                }

                GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromBitmap(rotatedBitmap))
                    .positionFromBounds(bounds.build())
            }

    val mapFeatures: Flow<List<MapData.Feature>> =
        combine(mapZoom, filterHandler.predicate, projection) { zoom, query, _ ->
            data.geoJson.features
                .filter { feature -> feature.geometry.type == FeatureType.Point }
                .filter { feature ->
                    val queryResult = query?.toPredicate()?.test(feature.properties.tags) ?: true

                    val featureZoom = feature.properties.zoomLevel
                    val min = featureZoom?.min?.toFloat() ?: minZoomLevel
                    val max = featureZoom?.max?.toFloat() ?: maxZoomLevel
                    val zoomResult = round(zoom) in min..max

                    queryResult && zoomResult
                }
        }

    fun createMapPinIcon(
        context: Context,
        feature: MapData.Feature,
        glyphDrawable: Drawable?,
    ): MapFeatureItem? {
        val style = feature.properties.style
        style?.type ?: return null
        val backgroundColor = style.markerColor?.toColorInt() ?: MapsColor.geoMap.point.marker
        val glyphColor = style.glyphColor?.toColorInt() ?: MapsColor.geoMap.point.glyph
        val selected = feature == markerHolder.selectedFeature.value

        val imageProperties = ImageProperties(
            style.type,
            backgroundColor,
            glyphColor,
            style.glyphName,
            selected,
        )

        return pinCache[imageProperties]?.let { MapFeatureItem(feature, it) } ?: run {
            return when (style.type) {
                "nativePin" -> {
                    val glyph = glyphDrawable
                        ?: ResourcesCompat.getDrawable(context.resources, R.drawable.circle, null)!!
                    createPinImage(context, glyph, imageProperties)
                }

                "dot" -> createDotImage(context, imageProperties)
                else -> null
            }?.let {
                pinCache[imageProperties] = it
                MapFeatureItem(feature, it)
            }
        }
    }

    internal fun createMapLabel(feature: MapData.Feature): MapItem.MapLabelItem? {
        val label = localizationService.getString(feature.properties.title) ?: return null
        val isSelected = feature == markerHolder.selectedFeature.value
        val labelItem = createLabelItem(feature, label, isSelected)

        return labelItem
    }

    internal fun holdMarker(marker: Marker?, feature: MapFeatureItem) {
        marker ?: return
        markerHolder.remove(feature)
        markerHolder.featureMarkers[marker] = feature
    }

    internal fun holdMarker(marker: Marker?, label: MapItem.MapLabelItem) {
        marker ?: return
        markerHolder.remove(label)
        markerHolder.labelMarkers[marker] = label
    }

    fun onDestroyView() {
        markerHolder.featureMarkers.clear()
        markerHolder.labelMarkers.clear()
    }

    fun onCreate(savedInstanceState: Bundle?, data: GeoMapLayoutData) {
        preselectedFeatureShown =
            savedInstanceState?.getBoolean(SAVED_PRESELECTED_FEATURE_SHOWN_KEY) ?: false
        lastMapViewState = savedInstanceState?.getBundle(SAVED_MAPVIEW_STATE_KEY)
        savedFiltering = savedInstanceState?.getKiboSerializable<FilteringInfo>(SAVED_FILTERING_KEY)
        savedSelectedFeatureId = savedInstanceState?.getString(SAVED_SELECTED_LOCATION_ID_KEY)
        this.data = data
    }

    fun onSaveInstanceState(outState: Bundle): Bundle {
        outState.putString(
            SAVED_SELECTED_LOCATION_ID_KEY,
            markerHolder.selectedFeature.value?.properties?.locationId
        )
        outState.putBoolean(SAVED_PRESELECTED_FEATURE_SHOWN_KEY, preselectedFeatureShown)
        outState.putBundle(SAVED_MAPVIEW_STATE_KEY, lastMapViewState)
        outState.putKiboSerializable(SAVED_FILTERING_KEY, getCurrentFilterState())
        return outState
    }

    fun selectFeature(feature: MapData.Feature) {
        markerHolder.selectedFeature.value = feature
    }

    fun deselectFeature(): MapData.Feature? {
        val feature = markerHolder.selectedFeature.value
        markerHolder.selectedFeature.value = null
        savedSelectedFeatureId = null

        return feature
    }

    companion object {
        const val SAVED_MAPVIEW_STATE_KEY = "savedMapviewStateKey"
        const val SAVED_FILTERING_KEY = "SAVED_FILTERING_KEY"
        const val SAVED_PRESELECTED_FEATURE_SHOWN_KEY = "PRESELECTED_FEATURE_SHOWN_KEY"
        const val SAVED_SELECTED_LOCATION_ID_KEY = "SELECTED_LOCATION_ID_KEY"
    }
}

internal data class ImageProperties(
    val type: String,
    val backgroundColor: Int,
    val glyphColor: Int,
    val glyphName: String?,
    val selected: Boolean,
)
