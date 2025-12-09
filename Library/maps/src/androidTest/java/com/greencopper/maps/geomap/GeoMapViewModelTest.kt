package com.greencopper.maps.geomap

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.greencopper.core.data.getKiboSerializable
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.imageservice.ImageResult
import com.greencopper.maps.geomap.GeoMapViewModel.Companion.SAVED_MAPVIEW_STATE_KEY
import com.greencopper.maps.geomap.GeoMapViewModel.Companion.SAVED_PRESELECTED_FEATURE_SHOWN_KEY
import com.greencopper.maps.geomap.GeoMapViewModel.Companion.SAVED_SELECTED_LOCATION_ID_KEY
import com.greencopper.maps.geomap.data.MapData
import com.greencopper.maps.geomap.data.MapData.Feature
import com.greencopper.maps.geomap.data.MapData.FeatureType
import com.greencopper.maps.geomap.ui.MapItem
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.core.*
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GeoMapViewModelTest {
    private val locationProvider = MockGeolocationProvider()
    private val locationService = MockLocationService()
    private val featureResolver = MockFeatureResolver()
    private val filteringHandler = MockFilteringHandler()
    private val imageService = MockImageService()
    private val localizationService = MockLocalizationService()
    private var context: Context

    private val viewModel = GeoMapViewModel(
        locationProvider,
        locationService,
        featureResolver,
        filteringHandler,
        imageService,
        localizationService
    )

    init {
        Toolkit.setupTest()
        bindProvider<BuildConfigProvider>(MockBuildConfigProvider())
        mockkStatic(Typeface::class)
        every { Typeface.create(any<Typeface>(), any()) } returns mockk()
        every { Typeface.create(any(), any(), any()) } returns mockk()

        context = InstrumentationRegistry.getInstrumentation().context
        MapsInitializer.initialize(context)
    }

    @Test
    fun requestPermissionIfNecessary_returnsForegroundPermission() {
        runTest {
            val resources: Resources = mockk()
            val activity: FragmentActivity = mockk()
            val appInfo: ApplicationInfo = mockk()
            every { resources.getString(any()) } returns "Title"
            every { resources.getString(any(), any()) } returns "Title"
            every { activity.resources } returns resources
            every { activity.applicationInfo } returns appInfo
            every { activity.packageManager } returns mockk()
            every { activity.packageName } returns "packageName"
            every { appInfo.loadLabel(any()) } returns "label"
            mockkStatic(Uri::class)
            every { Uri.parse(any()) } returns mockk()

            assertThat(
                viewModel.requestPermissionIfNecessary(activity).first()
            ).isEqualTo(locationService.foregroundPermission)
        }
    }

    @Test
    fun givenNoPreselectedLocationAndSavedSelectedFeatureId_whenGetPreselectedLocation_shouldReturnSelectedFeatureId() {
        val mapData = MapData(
            listOf(
                Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(locationId = "savedSelectedFeatureId")
                ),
                Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(locationId = "notId")
                ),
            ),
        )

        val layoutData = mockk<GeoMapLayoutData>(relaxed = true)
        every { layoutData.geoJson } returns mapData
        every { layoutData.preSelectedLocationId } returns null

        val savedInstanceState = Bundle()
        savedInstanceState.putString(SAVED_SELECTED_LOCATION_ID_KEY, "savedSelectedFeatureId")

        viewModel.onCreate(savedInstanceState, layoutData)

        assertThat(viewModel.getPreselectedLocation()?.properties?.locationId).isEqualTo("savedSelectedFeatureId")
    }

    @Test
    fun givenMapOverlayWithoutBitmap_getMapOverlays_shouldReturnEmptyList() {
        val mapData = MapData(
            listOf(
                Feature(
                    MapData.Geometry(FeatureType.Polygon, mockk(relaxed = true)),
                    mockk(relaxed = true)
                ),
            ),
        )
        val layoutData = mockk<GeoMapLayoutData>()
        every { layoutData.geoJson } returns mapData

        viewModel.onCreate(null, layoutData)

        runTest {
            assertThat(viewModel.getImageOverlays()).isEmpty()
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun givenMapOverlayWithBitmapAndLessThan2Coordinates_getMapOverlays_shouldReturnEmptyList() {
        val mapData = MapData(
            listOf(
                Feature(
                    MapData.Geometry(FeatureType.Polygon, mockk(relaxed = true)),
                    mockk(relaxed = true)
                ),
            ),
        )
        val layoutData = mockk<GeoMapLayoutData>()
        every { layoutData.geoJson } returns mapData

        viewModel.onCreate(null, layoutData)
        imageService.resultReturned = ImageResult.UNKNOWN(
            BitmapDrawable()
        )

        runTest {
            assertThat(viewModel.getImageOverlays()).isEmpty()
        }
    }

    @Test
    fun givenValidMapOverlay_getMapOverlays_shouldReturnListWithOverlay() {
        val context = InstrumentationRegistry.getInstrumentation().context
        MapsInitializer.initialize(context)

        val geometry = spyk(MapData.Geometry(FeatureType.Polygon, mockk(relaxed = true)))
        val coordinateList: List<LatLng> = listOf(LatLng(10.0, 10.0), LatLng(10.0, 10.0))
        every { geometry.getCoordinatesList() } returns coordinateList

        val feature = Feature(
            geometry,
            mockk(relaxed = true)
        )
        val mapData = MapData(listOf(feature))
        val layoutData = mockk<GeoMapLayoutData>()
        every { layoutData.geoJson } returns mapData

        viewModel.onCreate(null, layoutData)
        val drawable = BitmapDrawable(
            context.resources,
            BitmapFactory.decodeStream(context.assets.open("testContent/placeholder.png"))
        )
        val imageResult = ImageResult.UNKNOWN(
            drawable
        )
        imageService.resultReturned = imageResult

        runTest {
            assertThat(viewModel.getImageOverlays()).isNotEmpty()
        }
    }

    @Test
    fun givenFeatureWithoutStyle_createMapIcon_shouldReturnNull() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val properties = mockk<MapData.Properties>()
        every { properties.style } returns null
        val feature = Feature(mockk(), properties)

        assertThat(viewModel.createMapPinIcon(context, feature, null)).isNull()
    }

    @Test
    fun givenFeatureWithoutTitle_createMapLabel_shouldReturnNull() {
        val feature = mockk<Feature>(relaxed = true)
        every { feature.properties.title } returns null

        assertThat(viewModel.createMapLabel(feature)).isNull()
    }


    @Test
    fun givenFeatureWithTitle_createMapLabel_shouldReturnLabel() {
        val feature = mockk<Feature>(relaxed = true)
        every { feature.properties.title } returns "label"

        assertThat(viewModel.createMapLabel(feature)).isNotNull()
    }

    @Test
    fun givenNullMarkerAndFeature_holdMarker_shouldDoNothing() {
        val feature = mockk<MapItem.MapFeatureItem>(relaxed = true)

        val markersCount = viewModel.markerHolder.featureMarkers.size
        viewModel.holdMarker(null, feature)

        assertThat(markersCount).isEqualTo(viewModel.markerHolder.featureMarkers.size)
    }

    @Test
    fun givenMarkerAndFeature_holdMarker_shouldSaveMarkerInMarkerHolder() {
        val feature = mockk<MapItem.MapFeatureItem>(relaxed = true)
        val marker = mockk<Marker>()
        val markersCount = viewModel.markerHolder.featureMarkers.size

        viewModel.holdMarker(marker, feature)
        assertThat(viewModel.markerHolder.featureMarkers[marker]).isEqualTo(feature)
        assertThat(viewModel.markerHolder.featureMarkers.size).isEqualTo(markersCount + 1)
    }

    @Test
    fun givenNullMarkerAndLabel_holdMarker_shouldDoNothing() {
        val label = mockk<MapItem.MapLabelItem>(relaxed = true)

        val markersCount = viewModel.markerHolder.labelMarkers.size
        viewModel.holdMarker(null, label)

        assertThat(markersCount).isEqualTo(viewModel.markerHolder.labelMarkers.size)
    }

    @Test
    fun givenMarkerAndLabel_holdMarker_shouldSaveMarkerInMarkerHolder() {
        val label = mockk<MapItem.MapLabelItem>(relaxed = true)
        val marker = mockk<Marker>()
        val markersCount = viewModel.markerHolder.labelMarkers.size

        viewModel.holdMarker(marker, label)
        assertThat(viewModel.markerHolder.labelMarkers[marker]).isEqualTo(label)
        assertThat(viewModel.markerHolder.labelMarkers.size).isEqualTo(markersCount + 1)
    }

    @Test
    fun onDestroyView_shouldClearMakerHolders() {
        val feature = mockk<MapItem.MapFeatureItem>(relaxed = true)
        val label = mockk<MapItem.MapLabelItem>(relaxed = true)
        val marker = mockk<Marker>()

        viewModel.holdMarker(marker, label)
        viewModel.holdMarker(marker, feature)

        viewModel.onDestroyView()

        assertThat(viewModel.markerHolder.featureMarkers).isEmpty()
        assertThat(viewModel.markerHolder.labelMarkers).isEmpty()
    }

    @Test
    fun onSaveInstanceState_shouldSavePropertiesToBundle() {
        val bundle = Bundle()
        val lastState = Bundle()
        val feature = Feature(mockk(), MapData.Properties("locationId"))

        viewModel.markerHolder.selectedFeature.value = feature
        viewModel.preselectedFeatureShown = true
        viewModel.lastMapViewState = lastState

        val result = viewModel.onSaveInstanceState(bundle)

        assertThat(result.getString(SAVED_SELECTED_LOCATION_ID_KEY)).isEqualTo("locationId")
        assertThat(result.getBoolean(SAVED_PRESELECTED_FEATURE_SHOWN_KEY)).isTrue()
        assertThat(result.getBundle(SAVED_MAPVIEW_STATE_KEY)).isEqualTo(lastState)
        assertThat(result.getKiboSerializable<FilteringInfo>(SAVED_MAPVIEW_STATE_KEY)).isEqualTo(filteringHandler.currentStateToInfo)
    }

    @Test
    fun selectFeature_shouldSaveFeatureToMakerHolder() {
        val feature = mockk<Feature>()
        viewModel.selectFeature(feature)

        assertThat(viewModel.markerHolder.selectedFeature.value).isEqualTo(feature)
    }

    @Test
    fun deselectFeature_shouldRemoveFeatureFromMakerHolder() {
        val feature = mockk<Feature>()
        viewModel.selectFeature(feature)
        viewModel.deselectFeature()

        assertThat(viewModel.markerHolder.selectedFeature.value).isNull()
        assertThat(viewModel.savedSelectedFeatureId).isNull()
    }

}
