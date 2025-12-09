package com.greencopper.maps.geomap.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.maps.android.ktx.awaitMap
import com.greencopper.core.data.KiboSerializable.Companion.decodeFromString
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.imageservice.ImageService
import com.greencopper.interfacekit.metrics.LocationPermissionEvent
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.replace
import com.greencopper.interfacekit.ui.pxToDp
import com.greencopper.interfacekit.ui.setShadowColor
import com.greencopper.interfacekit.ui.shouldColorNavigationBar
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultButtonsNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import com.greencopper.kiba_maps.R
import com.greencopper.kiba_maps.databinding.GeoMapFragmentBinding
import com.greencopper.maps.colors.MapsColor
import com.greencopper.maps.geomap.GeoMapLayoutData
import com.greencopper.maps.geomap.GeoMapViewModel
import com.greencopper.maps.geomap.data.MapData
import com.greencopper.maps.locationdetail.LocationDetailInitializer
import com.greencopper.maps.locationdetail.ui.LocationDetailFragment
import com.greencopper.maps.metrics.geoMap
import com.greencopper.maps.metrics.geoMapPinClick
import com.greencopper.maps.textstyle.MapsTextStyle
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import java.util.concurrent.Executors

internal class GeoMapFragment : BottomSheetContainerFragment<GeoMapLayoutData>, RedirectableLayout {

    constructor(constructorData: GeoMapLayoutData) : super(constructorData)

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    override val binding: GeoMapFragmentBinding by viewBinding(
        GeoMapFragmentBinding::inflate
    )

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash
    override val screenColor: ScreenColor get() = MapsColor.geoMap

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var screenWidth: Float = 0f
    private var screenHeight: Float = 0f

    private val imageService: ImageService by App.lazy()
    private val routeController: RouteController by App.lazy()
    private val localizationService: LocalizationService by App.lazy()
    private val metricService: AggregateMetricsService by App.lazy()

    private val viewModel: GeoMapViewModel by viewModel {
        val map = mapOf(FilteringHandler.Mode.DEFAULT to data.filtering).takeIf { data.filtering != null } ?: emptyMap()
        listOf(FilteringHandler.Mode.DEFAULT, map)
    }

    private val allPointsCoordinates: List<LatLng> by lazy {
        data.geoJson.features.mapNotNull {
            if (it.geometry.type == MapData.FeatureType.Point)
                it.geometry.getPointCoordinates()
            else null
        }
    }

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultButtonsNavigationControlsHandler(
            this,
            binding.navigateBackButton,
            binding.navigateCloseButton,
            MapsColor.geoMap.topBar
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onCreate(savedInstanceState, data)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapview
        mapView.onCreate(viewModel.lastMapViewState)

        setupBottomSheet(binding.bottomSheetLocationDetail)
        setupMap()

        setupFiltering()
        setupSearchButton()
        setupMyCurrentLocationButton()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (viewModel.hasPermissionsGranted()) {
            listenToLocation()
        }
        lifecycleScope.launch {
            metricService.track(ScreenViewEvent(Screen.geoMap(data.analytics.screenName)))
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        viewModel.removeLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        val mapviewState = Bundle()
        mapView.onSaveInstanceState(mapviewState)
        viewModel.lastMapViewState = mapviewState
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        viewModel.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(viewModel.onSaveInstanceState(outState))
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (!viewModel.hasPermissionsGranted()) {
                activity?.let { activity ->
                    metricService.track(LocationPermissionEvent(viewModel.getAuthorizationStatus()))
                    if (viewModel.requestPermissionIfNecessary(activity).first()) {
                        listenToLocation()
                        adaptCameraToLocation()
                    }
                }
            } else {
                adaptCameraToLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun listenToLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            val googleMap = mapView.awaitMap()
            viewModel.requestLocationUpdates().collectLatest {

                googleMap.isMyLocationEnabled = it?.let {
                    data.camera.restrictedArea.contains(it.toLatLng())
                } ?: false
            }
        }
    }

    //warnings can be ignored since we are not using clustering/KML/etc.
    @SuppressLint("all")
    private fun setupMap() = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
        googleMap = mapView.awaitMap()
        val context = context ?: return@launch

        screenWidth = mapView.measuredWidth.pxToDp()
        screenHeight = mapView.measuredHeight.pxToDp()

        with(googleMap) {
            uiSettings.isRotateGesturesEnabled = data.camera.isRotateEnabled
            uiSettings.isCompassEnabled = false
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
            isIndoorEnabled = false
            // map style generated with https://mapstyle.withgoogle.com/
            val mapStyle =
                if (data.geoJson.features.any { it.geometry.type == MapData.FeatureType.Polygon })
                    R.raw.map_style_no_labels
                else
                    R.raw.map_style
            setMapStyle(MapStyleOptions.loadRawResourceStyle(context, mapStyle))

            viewModel.setMapZoomLevel(cameraPosition.zoom)
            setOnCameraIdleListener { onMapIdle(this) }

            setOnMarkerClickListener(this@GeoMapFragment::onMarkerClick)

            setOnMapClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    unselectPins()
                    setBottomSheetVisibility(false)
                }
            }
        }

        val camData = data.camera.initial
        viewModel.lastMapViewState ?: googleMap.moveCameraToInitialPosition(
            screenWidth,
            screenHeight,
            camData.center,
            camData.radius,
            camData.bearing
        )

        googleMap.limitMapMovement(data.camera.restrictedArea, screenWidth, screenHeight)
        googleMap.addBackgroundOverlay(data.hideMapBackgroundColor)

        viewModel.minZoomLevel = googleMap.minZoomLevel
        viewModel.maxZoomLevel = googleMap.maxZoomLevel
        viewModel.projection.value = googleMap.projection

        addMapOverlays {
            collectMapFeatures()
        }

        openPreselectedLocation()
    }

    private fun setupFiltering() = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
        if (data.filtering != null) {
            with(binding.filteringBar) {
                isVisible = true
                setup(
                    MapsColor.geoMap.filters,
                    MapsTextStyle.geoMap.filters,
                    transparentBackground = true,
                    viewLifecycleOwner.lifecycleScope
                )
                update(
                    viewModel.getFilteringBarData(this@GeoMapFragment, data.analytics.screenName)
                )
            }
        }
    }

    private suspend fun adaptCameraToLocation() {
        val googleMap = mapView.awaitMap()

        val myLocation = takeIf { viewModel.hasPermissionsGranted() }?.let {
            viewModel.getLastKnownLocation()?.let { myLocation ->
                LatLng(
                    myLocation.latitude,
                    myLocation.longitude,
                )
            }
        } ?: return

        val boundaries = data.camera.restrictedArea

        if (!boundaries.contains(myLocation)) {
            routeController.showAlert(
                localizationService.getString("maps.geoMap.out_of_bounds.title"),
                localizationService.getString("maps.geoMap.out_of_bounds.message"),
            )
            // if user is out of bounds when the press the Locate Me button, we no longer want to move the map
            return
        }

        val projectionBounds = googleMap.projection.visibleRegion.latLngBounds
        if (projectionBounds.contains(myLocation)) return

        val newBounds = LatLngBounds.builder().apply {
            //Find all currently visible pins
            allPointsCoordinates
                .filter { projectionBounds.contains(it) }
                .takeIf { it.isNotEmpty() }
                ?.forEach(::include)
                ?: run { include(projectionBounds.center) /*If no pins are currently visible, we use the center of the projection*/ }
            include(myLocation)
        }.build()

        val currentBearing = googleMap.cameraPosition.bearing

        googleMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder()
                    .zoom(newBounds.calculateZoomForBounds(screenWidth, screenHeight, currentBearing) * 0.98f)
                    .bearing(currentBearing)
                    .target(newBounds.center)
                    .build()
            )
        )
    }

    private fun setupSearchButton() = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
        data.search?.let { dataSearch ->
            val buttonColor = MapsColor.geoMap.searchButton
            with(binding.searchButton) {
                contentDescription = localizationService.getString("common.search")
                backgroundTintList = ColorStateList.valueOf(buttonColor.background)
                iconTint = ColorStateList.valueOf(buttonColor.icon)
                setShadowColor(buttonColor.shadow)

                setOnSafeClickListener {
                    routeController.resolveRouteLink(dataSearch.onTapRouteLink, this@GeoMapFragment)
                }
                isVisible = true
            }
        }
    }

    private fun setupMyCurrentLocationButton() = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
        binding.myCurrentLocationButton.isVisible = data.showUserLocation

        if (data.showUserLocation) {
            val buttonColor = MapsColor.geoMap.userLocationButton
            with(binding.myCurrentLocationButton) {
                contentDescription = localizationService.getString("maps.geo_map.my_location.accessibility_label")
                backgroundTintList = ColorStateList.valueOf(buttonColor.background)
                iconTint = ColorStateList.valueOf(buttonColor.icon)
                setShadowColor(buttonColor.shadow)

                setOnSafeClickListener { requestCurrentLocation() }
            }
        }
    }

    private fun openPreselectedLocation() {
        viewModel.getPreselectedLocation()?.let { feature ->
            onPinTap(feature)
            if (feature.geometry.type == MapData.FeatureType.Point) {
                googleMap.moveCamera(feature.geometry.getPointCoordinates())
            }
        }
    }

    override fun restoreData(encodedData: String): GeoMapLayoutData = decodeFromString(encodedData)

    private fun addMapOverlays(callback: () -> Unit) = viewLifecycleOwner.lifecycleScope.launch(context = Dispatchers.IO) {
        val overlays = viewModel.getImageOverlays()
        withContext(Dispatchers.Main) {
            googleMap.addImageOverlays(overlays)
            callback()
        }
    }

    private fun collectMapFeatures() = viewLifecycleOwner.lifecycleScope.launch(context = Dispatchers.IO) {
        viewModel.mapFeatures.collectLatest { features ->
            removePointsFromMap(features)

            val shownFeatures = viewModel.markerHolder.featureMarkers.values.map { it.feature }
            val featuresToShow = features.subtract(shownFeatures.toSet())
            val pool = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
            val addJobs = featuresToShow.map { feature ->
                viewLifecycleOwner.lifecycleScope.launch(pool) { addPointToMap(feature) }
            }
            addJobs.joinAll()
            pool.close()

            features.forEach { addLabelToMap(it) }
        }
    }

    private suspend fun removePointsFromMap(
        featuresToKeep: List<MapData.Feature>,
    ) {
        val itemsToRemove = viewModel.markerHolder.getAllMapItems().filter {
            !featuresToKeep.contains(it.feature)
        }

        withContext(Dispatchers.Main) {
            itemsToRemove.map {
                viewModel.markerHolder.remove(it)
            }
        }
    }

    private suspend fun addPointToMap(
        feature: MapData.Feature,
    ) {
        val style = feature.properties.style
        imageService.getImageDrawable(
            style?.glyphName,
            hideIfUnknown = true,
            hideIfLoading = true,
        ).collect { imageResult ->
            val featureItem = viewModel.createMapPinIcon(requireContext(), feature, imageResult.drawable) ?: return@collect

            val markerOptions = MarkerOptions()
                .icon(featureItem.bitmapDescriptor)
                .position(feature.geometry.getPointCoordinates())
                .anchor(0.5f, 0.5f)
                .contentDescription(localizationService.getString(featureItem.feature.properties.title))

            withContext(Dispatchers.Main) {
                val marker = googleMap.addMarker(markerOptions)
                viewModel.holdMarker(marker, featureItem)
            }
        }
    }

    private suspend fun addLabelToMap(feature: MapData.Feature) {
        val labelItem = viewModel.createMapLabel(feature) ?: return

            val markerOptions = MarkerOptions()
                .icon(labelItem.bitmapDescriptor)
                .position(labelItem.position)
                .anchor(0.5f, 0f)
                .contentDescription(localizationService.getString(labelItem.feature.properties.title))

            withContext(Dispatchers.Main) {
                val noCollision = !viewModel.markerHolder.collidesWithAnything(googleMap.projection, labelItem)
                val isSelected = viewModel.markerHolder.selectedFeature.value == feature
                val isAlreadyShown = viewModel.markerHolder.labelMarkers.containsValue(labelItem)
                if (noCollision || isSelected) {
                    if (!isAlreadyShown) {
                        val marker = googleMap.addMarker(markerOptions)
                        viewModel.holdMarker(marker, labelItem)
                    }
                } else if (isAlreadyShown) {
                    viewModel.markerHolder.remove(labelItem)
                }
            }
        }

    private fun onPinTap(feature: MapData.Feature) = viewLifecycleOwner.lifecycleScope.launch {
        if (feature.properties.onTap == null) return@launch

        unselectPins()
        selectPin(feature)

        val route = feature.properties.onTap
        viewModel.selectedRoute.value = route

        when (route) {
            is Route.Present -> route.feature
            is Route.Push -> route.feature
            else -> null
        }?.takeIf { it.key == LocationDetailInitializer.key }?.let {
            viewModel.getFragmentLayout(it)?.let { layout ->
                (layout as? LocationDetailFragment)?.setParentFeatureInfo(data.featureInfo)
                layout.shouldColorNavigationBar = false
                childFragmentManager.replace(com.greencopper.interfacekit.R.id.bottom_sheet_fragment_holder, layout)
            }
        } ?: run {
            routeController.resolve(route, this@GeoMapFragment)
        }

        setBottomSheetVisibility(true)
    }

    private suspend fun selectPin(feature: MapData.Feature) {
        viewModel.selectFeature(feature)
        addPointToMap(feature)
        addLabelToMap(feature)
    }

    override fun onHideBottomSheet() {
        if (viewModel.selectedRoute.value != null) {
            viewModel.selectedRoute.value = null
            unselectPins()
        }
    }

    private fun unselectPins() {
        viewModel.deselectFeature()?.let {
            viewLifecycleOwner.lifecycleScope.launch(context = Dispatchers.IO) {
                addPointToMap(it)
                addLabelToMap(it)
            }
        }
    }

    private fun onMapIdle(map: GoogleMap) {
        viewModel.projection.value = map.projection
        viewModel.setMapZoomLevel(map.cameraPosition.zoom)
    }

    private fun onMarkerClick(marker: Marker): Boolean {
        runBlocking {
            viewModel.markerHolder.getFeatureFromMarker(marker)?.let { feature ->
                feature.properties.analytics?.let { analytics ->
                    metricService.track(
                        PinClickEvent(
                            analytics.itemName,
                            analytics.itemId.toString(),
                            data.analytics.screenName
                        )
                    )
                }

                onPinTap(feature)
                return@let true
            }
        }
        return false
    }
}

internal class PinClickEvent(
    private val itemName: String,
    private val itemId: String,
    private val screenName: String,
) : MappedMetrics {

    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.itemName to itemName,
            EventParameter.itemId to itemId,
            EventParameter.screenName to screenName
        )

        provider.track(EventName.geoMapPinClick(), parameters)
    }
}
