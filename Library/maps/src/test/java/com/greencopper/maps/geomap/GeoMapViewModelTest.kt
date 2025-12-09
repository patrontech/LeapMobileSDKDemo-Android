package com.greencopper.maps.geomap

import android.graphics.Typeface
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.interfacekit.filtering.MockFilteringPredicateComputed
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.maps.geomap.data.MapData
import com.greencopper.maps.geomap.data.MapData.FeatureType
import com.greencopper.maps.geomap.ui.toLatLng
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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class GeoMapViewModelTest {

    private val locationProvider = MockGeolocationProvider()
    private val locationService = MockLocationService()
    private val featureResolver = MockFeatureResolver()
    private val filteringHandler = MockFilteringHandler()
    private val imageService = MockImageService()
    private val localizationService = MockLocalizationService()

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
    }

    @Test
    fun givenPreselectedLocation_whenGetPreselectedLocation_shouldReturnId() {
        val mapData = MapData(
            listOf(
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(locationId = "id")
                ),
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(locationId = "notId")
                ),
            ),
        )
        val layoutData = mockk<GeoMapLayoutData>(relaxed = true)
        every { layoutData.geoJson } returns mapData
        every { layoutData.preSelectedLocationId } returns "id"

        viewModel.onCreate(null, layoutData)

        assertThat(viewModel.getPreselectedLocation()?.properties?.locationId).isEqualTo("id")
    }

    @Test
    fun givenNoPreselectedLocation_whenGetPreselectedLocation_shouldReturnNull() {
        val mapData = MapData(
            listOf(
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(locationId = "id")
                ),
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(locationId = "notId")
                ),
            ),
        )
        val layoutData = mockk<GeoMapLayoutData>(relaxed = true)
        every { layoutData.geoJson } returns mapData
        every { layoutData.preSelectedLocationId } returns null

        viewModel.onCreate(null, layoutData)

        assertThat(viewModel.getPreselectedLocation()).isNull()
    }

    @Test
    fun givenNoPreselectedLocationAndSelectedFeature_whenGetPreselectedLocation_shouldReturnSelectedFeature() {
        val selectedFeature = MapData.Feature(
            MapData.Geometry(FeatureType.Point, mockk()),
            MapData.Properties(locationId = "id")
        )
        val mapData = MapData(
            listOf(
                selectedFeature,
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(locationId = "notId")
                ),
            ),
        )
        val layoutData = mockk<GeoMapLayoutData>(relaxed = true)
        every { layoutData.geoJson } returns mapData
        every { layoutData.preSelectedLocationId } returns null
        viewModel.markerHolder.selectedFeature.value = selectedFeature

        viewModel.onCreate(null, layoutData)

        assertThat(viewModel.getPreselectedLocation()?.properties?.locationId).isEqualTo(selectedFeature.properties.locationId)
    }

    @Test
    fun lastKnownLocation_isEqualTo_providersCurrentLocation() {
        runTest {
            locationProvider.currentLocation?.latitude = 60.1
            locationProvider.currentLocation?.longitude = 50.1
            val location = viewModel.getLastKnownLocation()
            assertThat(location?.toLatLng()).isEqualTo(locationProvider.currentLocation?.toLatLng())
        }
    }

    @Test
    fun requestLocationUpdates_isEqualTo_providersCurrentLocation() {
        runTest {
            locationProvider.currentLocation?.latitude = 60.1
            locationProvider.currentLocation?.longitude = 50.1
            val location = viewModel.requestLocationUpdates().first()
            assertThat(location?.toLatLng()).isEqualTo(locationProvider.currentLocation?.toLatLng())
        }
    }

    @Test
    fun permissionNotGranted_hasPermissionsGranted_returnsFalse() {
        locationService.foregroundPermission = false
        assertThat(viewModel.hasPermissionsGranted()).isFalse
    }

    @Test
    fun permissionGranted_hasPermissionsGranted_returnsTrue() {
        locationService.foregroundPermission = true
        assertThat(viewModel.hasPermissionsGranted()).isTrue
    }

    @Test
    fun getAuthorizationStatus_returnsAuthStatus() {
        val status1 = AuthorizationStatus.AuthorizedAlways
        locationService.currentAuthStatus = status1
        assertThat(viewModel.getAuthorizationStatus()).isEqualTo(status1)

        val status2 = AuthorizationStatus.Denied
        locationService.currentAuthStatus = status2
        assertThat(viewModel.getAuthorizationStatus()).isEqualTo(status2)
    }

    @Test
    fun getFragmentLayout_returnsLayout() {
        val layout = Layout()
        featureResolver.layout = layout
        assertThat(viewModel.getFragmentLayout(mockk())).isEqualTo(layout)
    }

    @Test
    fun noLayout_getFragmentLayout_returnsNull() {
        assertThat(viewModel.getFragmentLayout(mockk())).isNull()
    }

    @Test
    @DisplayName("Given there are no filters, When calling getFilteringBarData, Then it should return null")
    fun getFilteringBarDataReturnsNull() {
        filteringHandler.mockedFilteringBarData = FilteringBarData(emptyList())
        assertThat(viewModel.getFilteringBarData(mockk(), "")).isEqualTo(null)
    }

    @Test
    @DisplayName("Given there are no filters, When calling getFilteringBarData, Then it should return null")
    fun getFilteringBarData_returnsFilteringBarData() {
        val filter = FilteringBarData.Filter("1", 0, "testFilter", false, false, { })
        filteringHandler.mockedFilteringBarData = FilteringBarData(listOf(filter))
        assertThat(viewModel.getFilteringBarData(mockk(), "")).isEqualTo(filteringHandler.mockedFilteringBarData)
        assertThat(
            viewModel.getFilteringBarData(
                mockk(),
                ""
            )
        ).isEqualTo(filteringHandler.mockedFilteringBarData)
    }

    @Test
    fun getCurrentFilterState_returnsFilteringInfo() {
        assertThat(viewModel.getCurrentFilterState()).isEqualTo(filteringHandler.currentStateToInfo)
    }

    @Test
    fun givenNoMapOverlays_getMapOverlays_shouldReturnEmptyList() {
        runTest {
            viewModel.onCreate(null, mockk(relaxed = true))
            assertThat(viewModel.getImageOverlays()).isEmpty()
        }
    }

    @Test
    fun givenMatchesQuery_getMapPins_returnsAllFeatures() {
        filteringHandler.mockedPredicate = MockFilteringPredicateComputed()
        val mapData = MapData(
            listOf(
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = null, max = 10))
                ),
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = null, max = 10))
                ),
            ),
        )
        val layoutData = mockk<GeoMapLayoutData>()
        every { layoutData.geoJson } returns mapData

        viewModel.onCreate(null, layoutData)

        viewModel.setMapZoomLevel(1f)
        runTest {
            assertThat(viewModel.mapFeatures.first().size).isEqualTo(2)
        }
    }

    @Test
    fun givenDoesntMatchQuery_getMapPins_returnsNoFeatures() {
        filteringHandler.mockedPredicate = MockFilteringPredicateComputed(predicateResult = false)
        val mapData = MapData(
            listOf(
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = null, max = 10))
                ),
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = null, max = 10))
                ),
            ),
        )
        val layoutData = mockk<GeoMapLayoutData>()
        every { layoutData.geoJson } returns mapData

        viewModel.onCreate(null, layoutData)

        viewModel.setMapZoomLevel(1f)

        runTest {
            assertThat(viewModel.mapFeatures.first().size).isEqualTo(0)
        }
    }

    @Test
    fun givenNullPredicate_getMapPins_returnsGeoJsonFeatures() {
        filteringHandler.mockedPredicate = null
        val mapData = MapData(
            listOf(
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = null, max = 10))
                ),
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = null, max = 10))
                ),
            ),
        )

        val layoutData = mockk<GeoMapLayoutData>()
        every { layoutData.geoJson } returns mapData

        viewModel.onCreate(null, layoutData)

        viewModel.setMapZoomLevel(1f)
        runTest {
            assertThat(viewModel.mapFeatures.first()).isEqualTo(mapData.features)
        }
    }

    @Test
    fun givenMinZoomLevel_getMapPins_returnsFeaturesInRange() {
        val mapData = MapData(
            listOf(
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = 8, max = null))
                ),
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = 10, max = null))
                ),
            )
        )

        val layoutData = mockk<GeoMapLayoutData>()
        every { layoutData.geoJson } returns mapData

        viewModel.onCreate(null, layoutData)

        runTest {
            viewModel.setMapZoomLevel(8f)
            assertThat(viewModel.mapFeatures.first()).hasSize(1)
            viewModel.setMapZoomLevel(9f)
            assertThat(viewModel.mapFeatures.first()).hasSize(1)
            viewModel.setMapZoomLevel(10f)
            assertThat(viewModel.mapFeatures.first()).hasSize(2)
            viewModel.setMapZoomLevel(11f)
            assertThat(viewModel.mapFeatures.first()).hasSize(2)
        }
    }

    @Test
    fun givenMaxZoomLevel_getMapPins_returnsFeaturesInRange() {
        val mapData = MapData(
            listOf(
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = null, max = 10))
                ),
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = null, max = 12))
                ),
            )
        )

        val layoutData = mockk<GeoMapLayoutData>()
        every { layoutData.geoJson } returns mapData

        viewModel.onCreate(null, layoutData)

        runTest {
            viewModel.setMapZoomLevel(10f)
            assertThat(viewModel.mapFeatures.first()).hasSize(2)
            viewModel.setMapZoomLevel(11f)
            assertThat(viewModel.mapFeatures.first()).hasSize(1)
            viewModel.setMapZoomLevel(12f)
            assertThat(viewModel.mapFeatures.first()).hasSize(1)
            viewModel.setMapZoomLevel(13f)
            assertThat(viewModel.mapFeatures.first()).hasSize(0)
        }
    }

    @Test
    fun givenZoomLevel_getMapPins_returnsFeaturesInRange() {
        val mapData = MapData(
            listOf(
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = 8, max = 12))
                ),
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = 9, max = 10))
                ),
                MapData.Feature(
                    MapData.Geometry(FeatureType.Point, mockk()),
                    MapData.Properties(zoomLevel = MapData.ZoomLevel(min = 10, max = 12))
                ),
            )
        )

        val layoutData = mockk<GeoMapLayoutData>()
        every { layoutData.geoJson } returns mapData

        viewModel.onCreate(null, layoutData)

        runTest {
            viewModel.setMapZoomLevel(7f)
            assertThat(viewModel.mapFeatures.first()).hasSize(0)
            viewModel.setMapZoomLevel(8f)
            assertThat(viewModel.mapFeatures.first()).hasSize(1)
            viewModel.setMapZoomLevel(9f)
            assertThat(viewModel.mapFeatures.first()).hasSize(2)
            viewModel.setMapZoomLevel(10f)
            assertThat(viewModel.mapFeatures.first()).hasSize(3)
            viewModel.setMapZoomLevel(11f)
            assertThat(viewModel.mapFeatures.first()).hasSize(2)
            viewModel.setMapZoomLevel(12f)
            assertThat(viewModel.mapFeatures.first()).hasSize(2)
        }
    }

    @Test
    fun givenNoZoomLevel_getMapPins_returnsAllFeatures() {
        val mapData = MapData(
            listOf(
                MapData.Feature(MapData.Geometry(FeatureType.Point, mockk()), MapData.Properties(zoomLevel = null)),
                MapData.Feature(MapData.Geometry(FeatureType.Point, mockk()), MapData.Properties(zoomLevel = null)),
                MapData.Feature(MapData.Geometry(FeatureType.Point, mockk()), MapData.Properties(zoomLevel = null)),
            )
        )

        val layoutData = mockk<GeoMapLayoutData>()
        every { layoutData.geoJson } returns mapData

        viewModel.onCreate(null, layoutData)

        runTest {
            viewModel.setMapZoomLevel(7f)
            assertThat(viewModel.mapFeatures.first()).hasSize(3)
            viewModel.setMapZoomLevel(10f)
            assertThat(viewModel.mapFeatures.first()).hasSize(3)
            viewModel.setMapZoomLevel(15f)
            assertThat(viewModel.mapFeatures.first()).hasSize(3)
        }
    }
}
