package com.greencopper.core.location.manager

import android.location.Location
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.TestLocalStorageContainer
import com.greencopper.core.localstorage.core
import com.greencopper.core.location.localstorage.location
import com.greencopper.core.location.recipe.Region
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.core.MockGeolocationProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

internal class ConcreteLocationManagerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val localStorageContainer = TestLocalStorageContainer()
    private val lazyLocalStorage = LazyResolver.adhoc(
        LocalStorage(
            project = "testProject",
            localStorageContainer = localStorageContainer
        )
    )
    private val geolocationProvider = MockGeolocationProvider()
    private val remoteStateDispatcher = MockRemoteStateDispatcher(App.resolve())

    private val region1 = Region(
        id = 1,
        name = "region1",
        location = createLocation(1.0, 1.0),
        radiusInMeters = 10
    )
    private val region2 = Region(
        id = 2,
        name = "region2",
        location = createLocation(2.0, 2.0),
        radiusInMeters = 20
    )
    private val regionCurrentlyIn = Region(
        id = 10,
        name = "region10",
        location = createLocation(10.0, 10.0),
        radiusInMeters = 100
    )

    private val locationManager = ConcreteLocationManager(
        lazyLocalStorage = lazyLocalStorage,
        geolocationProvider = geolocationProvider,
        remoteStateDispatcher = remoteStateDispatcher,
        scope = testScope
    )

    override fun afterEach() {}

    @Test
    fun startMonitoringNewRegionIn_shouldAddToMonitoredRegions_andDispatch() {
        //when
        runTest {
            geolocationProvider.currentLocation?.longitude = 10.0
            geolocationProvider.currentLocation?.latitude = 10.0
            locationManager.startMonitoring(regionCurrentlyIn)
        }

        //then
        assertThat(geolocationProvider.addGeofenceCalled).isTrue
        assertThat(locationManager.monitoredRegions).containsOnly(regionCurrentlyIn)
        assertThat(remoteStateDispatcher.dispatchedEntry).isNotNull
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).containsOnly(
            regionCurrentlyIn.id
        )
    }

    @Test
    fun startMonitoringNewRegionOut_shouldAddToMonitoredRegions_withoutDispatch() {
        //when
        runTest {
            locationManager.startMonitoring(region2)
        }

        //then
        assertThat(geolocationProvider.addGeofenceCalled).isTrue
        assertThat(locationManager.monitoredRegions).containsOnly(region2)
        assertThat(remoteStateDispatcher.dispatchedEntry).isNull()
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).isEmpty()
    }

    @Test
    fun startMonitoringAlreadyAddedRegion_shouldntAddRegion_norDispatch() {
        runTest {
            //given
            locationManager.startMonitoring(region1)
            geolocationProvider.resetVerifiers()

            //when
            locationManager.startMonitoring(region1)
        }

        //then
        assertThat(geolocationProvider.addGeofenceCalled).isFalse
        assertThat(locationManager.monitoredRegions).containsOnly(region1)
        assertThat(remoteStateDispatcher.dispatchedEntry).isNull()
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).isEmpty()
    }

    @Test
    fun stopMonitoringRegionOut_shouldRemoveRegion_WithoutDispatch() {
        runTest {
            //given
            locationManager.startMonitoring(region1)
            locationManager.startMonitoring(region2)
            locationManager.updateState(region2.id, true)
            assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).containsOnly(
                region2.id
            )

            geolocationProvider.resetVerifiers()
            remoteStateDispatcher.dispatchedEntry = null

            //when
            locationManager.stopMonitoring(region1)
        }

        //then
        assertThat(geolocationProvider.removeGeofenceCalled).isTrue
        assertThat(remoteStateDispatcher.dispatchedEntry).isNull()
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).containsOnly(
            region2.id
        )
        assertThat(locationManager.monitoredRegions).containsOnly(region2)
    }

    @Test
    fun stopMonitoringRegionIn_shouldRemoveRegion_WithDispatch() {
        runTest {
            //given
            locationManager.startMonitoring(region1)
            locationManager.startMonitoring(region2)
            locationManager.updateState(region1.id, true)
            locationManager.updateState(region2.id, true)
            assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).contains(
                region1.id,
                region2.id
            )

            geolocationProvider.resetVerifiers()
            remoteStateDispatcher.dispatchedEntry = null

            //when
            locationManager.stopMonitoring(region1)
        }

        //then
        assertThat(geolocationProvider.removeGeofenceCalled).isTrue
        assertThat(remoteStateDispatcher.dispatchedEntry).isNotNull
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).containsOnly(
            region2.id
        )
        assertThat(locationManager.monitoredRegions).containsOnly(region2)
    }

    @Test
    fun stopMonitoringUnknownRegion_shouldntRemoveRegion() {
        runTest {
            //given
            locationManager.startMonitoring(region1)
            geolocationProvider.resetVerifiers()

            //when
            locationManager.stopMonitoring(region2)
        }

        //then
        assertThat(geolocationProvider.removeGeofenceCalled).isFalse
        assertThat(locationManager.monitoredRegions).containsOnly(region1)
    }

    @Test
    fun updateStateOfMonitoredRegionOut_toEntered_shouldUpdate() {
        runTest {
            //given
            locationManager.startMonitoring(region1)

            geolocationProvider.resetVerifiers()

            //when
            locationManager.updateState(region1.id, true)
        }

        //then
        assertThat(remoteStateDispatcher.dispatchedEntry).isNotNull
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).containsOnly(
            region1.id
        )
        assertThat(locationManager.monitoredRegions).containsOnly(region1)
    }

    @Test
    fun updateStateOfMonitoredRegionIn_toEntered_shouldNotUpdate() {
        runTest {
            //given
            locationManager.startMonitoring(region1)
            locationManager.updateState(region1.id, true)

            geolocationProvider.resetVerifiers()
            remoteStateDispatcher.dispatchedEntry = null
            assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).containsOnly(
                region1.id
            )

            //when
            locationManager.updateState(region1.id, true)
        }

        //then
        assertThat(remoteStateDispatcher.dispatchedEntry).isNull()
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).containsOnly(
            region1.id
        )
        assertThat(locationManager.monitoredRegions).containsOnly(region1)
    }

    @Test
    fun updateStateOfMonitoredRegionIn_toLeave_shouldUpdate() {
        runTest {
            //given
            locationManager.startMonitoring(region1)
            locationManager.updateState(region1.id, true)

            geolocationProvider.resetVerifiers()
            remoteStateDispatcher.dispatchedEntry = null
            assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).containsOnly(
                region1.id
            )

            //when
            locationManager.updateState(region1.id, false)
        }

        //then
        assertThat(remoteStateDispatcher.dispatchedEntry).isNotNull
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).isEmpty()
        assertThat(locationManager.monitoredRegions).containsOnly(region1)
    }

    @Test
    fun updateStateOfMonitoredRegionOut_toLeave_shouldNotUpdate() {
        runTest {
            //given
            locationManager.startMonitoring(region1)

            geolocationProvider.resetVerifiers()

            //when
            locationManager.updateState(region1.id, false)
        }

        //then
        assertThat(remoteStateDispatcher.dispatchedEntry).isNull()
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).isEmpty()
        assertThat(locationManager.monitoredRegions).containsOnly(region1)
    }

    @Test
    fun updateStateOfUnknownRegion_shouldNotUpdate() {
        runTest {
            //when
            locationManager.updateState(123, true)
        }

        //then
        assertThat(remoteStateDispatcher.dispatchedEntry).isNull()
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).isEmpty()
        assertThat(locationManager.monitoredRegions).isEmpty()
    }

    @Test
    fun addMonitoredRegion_withoutLocation_shouldClear() {
        runTest {
            //given
            geolocationProvider.currentLocation?.longitude = 10.0
            geolocationProvider.currentLocation?.latitude = 10.0
            locationManager.startMonitoring(regionCurrentlyIn)

            geolocationProvider.resetVerifiers()
            geolocationProvider.currentLocation = null
            remoteStateDispatcher.dispatchedEntry = null

            //when
            locationManager.startMonitoring(region2)
        }

        //then
        assertThat(remoteStateDispatcher.dispatchedEntry).isNotNull
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).isEmpty()
        assertThat(locationManager.monitoredRegions).containsOnly(regionCurrentlyIn, region2)
    }

    @Test
    fun resetMonitoring_withNewRegionsIn_shouldUpdate() {
        //given
        val region4 = region2.copy(id = 4)
        runTest {
            locationManager.startMonitoring(region1)
            locationManager.startMonitoring(region2)
            locationManager.updateState(region1.id, true)
            locationManager.updateState(region2.id, true)

            geolocationProvider.resetVerifiers()
            remoteStateDispatcher.dispatchedEntry = null

            //when
            geolocationProvider.currentLocation?.longitude = 10.0
            geolocationProvider.currentLocation?.latitude = 10.0
            locationManager.resetMonitoring(listOf(regionCurrentlyIn, region4))
        }

        //then
        assertThat(geolocationProvider.removeAllGeofencesCalled).isTrue
        assertThat(geolocationProvider.addGeofencesCalled).isTrue
        assertThat(locationManager.monitoredRegions).contains(regionCurrentlyIn, region4)
        assertThat(remoteStateDispatcher.dispatchedEntry).isNotNull
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).containsOnly(
            regionCurrentlyIn.id
        )
    }

    @Test
    fun resetMonitoring_withNoNewRegions_shouldUpdate() {
        runTest {
            //given
            locationManager.startMonitoring(region1)
            locationManager.startMonitoring(region2)
            locationManager.updateState(region1.id, true)
            locationManager.updateState(region2.id, true)

            geolocationProvider.resetVerifiers()
            remoteStateDispatcher.dispatchedEntry = null

            //when
            locationManager.resetMonitoring(emptyList())
        }

        //then
        assertThat(geolocationProvider.removeAllGeofencesCalled).isTrue
        assertThat(geolocationProvider.addGeofencesCalled).isFalse
        assertThat(locationManager.monitoredRegions).isEmpty()
        assertThat(remoteStateDispatcher.dispatchedEntry).isNotNull
        assertThat(lazyLocalStorage.resolve().app.core.location.currentRegions.value).isEmpty()
    }

    private fun createLocation(latitude: Double, longitude: Double) = mockk<Location>().apply {
        every { getLatitude() } answers { latitude }
        every { getLongitude() } answers { longitude }

        every { distanceTo(any()) } answers {
            val secondLocation = it.invocation.args.first() as Location
            (acos(
                sin(latitude) * sin(secondLocation.latitude) + cos(latitude) * cos(secondLocation.latitude) * cos(
                    secondLocation.longitude - longitude
                )
            ) * 6371).toFloat()
        }
    }
}
