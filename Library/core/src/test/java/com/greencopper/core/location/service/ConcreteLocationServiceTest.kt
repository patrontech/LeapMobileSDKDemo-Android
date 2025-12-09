package com.greencopper.core.location.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.location.LocationConfigurationHolder
import com.greencopper.core.location.localstorage.location
import com.greencopper.core.location.manager.LocationManager
import com.greencopper.core.location.recipe.*
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.core.MockPermissionManager
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class ConcreteLocationServiceTest : CoroutineTest(StandardTestDispatcher()) {

    init {
        Toolkit.setupTest()
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk()
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setData((any())) } returns mockk()
    }

    private val context: Context = mockk(relaxed = true)
    private val locationConfigurationHolder = LocationConfigurationHolder()
    private val locationManager: LocationManager = mockk(relaxed = true)
    private val currentProjectTagProvider = object : CurrentProjectTagProvider {
        override val currentProject: String = "project"
        override val currentProjectFlow: StateFlow<String?> = MutableStateFlow(currentProject)

    }
    private val buildConfigProvider = MockBuildConfigProvider()
    private val localStorage = LocalStorage("project")
    private val activity: FragmentActivity = mockk()
    private val mockPermissionManager = MockPermissionManager()

    private val locationService: ConcreteLocationService = spyk(
        ConcreteLocationService(
            context,
            mockPermissionManager,
            locationManager,
            locationConfigurationHolder,
            currentProjectTagProvider,
            buildConfigProvider,
            MockLocalizationService(),
            LazyResolver.adhoc(localStorage),
            testScope,
        ),
        recordPrivateCalls = true
    )

    override fun afterEach() {}

    @Test
    fun whenAboveApiQ_requiresBackgroundPermission() {
        //given
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.Q

        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_BACKGROUND_LOCATION] =
            false

        //then
        assertThat(locationService.hasFineForegroundPermission()).isTrue
        assertThat(locationService.hasBackgroundAndForegroundPermission()).isFalse
    }

    @Test
    fun whenAboveApiQ_withPermissionsGranted_shouldHavePermissions() {
        //given
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.Q

        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_BACKGROUND_LOCATION] =
            true

        //then
        assertThat(locationService.hasFineForegroundPermission()).isTrue
        assertThat(locationService.hasBackgroundAndForegroundPermission()).isTrue
    }

    @Test
    fun whenRequestPermissionsDenied_returnsFalse() {
        runTest {
            //when
            val result = locationService.requestPermissions(
                activity = activity,
                rationalePanelConfig = locationService.defaultRationalePanelConfig,
                settingsPanelConfig = locationService.defaultSettingsPanelConfig,
                needsBackgroundLocation = true
            ).first()

            //then
            assertThat(result).isFalse
        }
    }

    @Test
    fun whenCoarseGranted_shouldHavePermissions() {
        //given
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            false

        //then
        assertThat(locationService.hasOneForegroundPermission()).isTrue
    }

    @Test
    fun whenFineGranted_shouldHavePermissions() {
        //given
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            false
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            true

        //then
        assertThat(locationService.hasOneForegroundPermission()).isTrue
    }

    @Test
    fun whenFineAndCoarseGranted_shouldHavePermissions() {
        //given
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            true

        //then
        assertThat(locationService.hasOneForegroundPermission()).isTrue
    }

    @Test
    fun whenRequestPermissionsAboveO_withoutBackground_withGranted_shouldReturnsTrue() {
        //given
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.R

        mockPermissionManager.askedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            true
        mockPermissionManager.askedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            true
        mockPermissionManager.askedPermissionsMockMap[Manifest.permission.ACCESS_BACKGROUND_LOCATION] =
            false

        //when
        runTest {
            val result = locationService.requestPermissions(
                activity = activity,
                rationalePanelConfig = locationService.defaultRationalePanelConfig,
                settingsPanelConfig = locationService.defaultSettingsPanelConfig,
                needsBackgroundLocation = false
            ).first()

            //then
            assertThat(result).isTrue
        }
    }

    @Test
    fun whenRequestPermissionsAboveO_withBackground_withGranted_shouldReturnsTrue() {
        //given
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.R

        mockPermissionManager.askedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            true
        mockPermissionManager.askedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            true
        mockPermissionManager.askedPermissionsMockMap[Manifest.permission.ACCESS_BACKGROUND_LOCATION] =
            true

        //when
        runTest {
            val result = locationService.requestPermissions(
                activity = activity,
                rationalePanelConfig = locationService.defaultRationalePanelConfig,
                settingsPanelConfig = locationService.defaultSettingsPanelConfig,
                needsBackgroundLocation = true
            ).first()

            //then
            assertThat(result).isTrue
        }
    }

    @Test
    fun whenRequestPermissionsAboveO_withBackground_withoutGranted_shouldReturnsFalse() {
        //given
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.R

        mockPermissionManager.askedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            false
        mockPermissionManager.askedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            false
        mockPermissionManager.askedPermissionsMockMap[Manifest.permission.ACCESS_BACKGROUND_LOCATION] =
            true

        //when
        runTest {
            val result = locationService.requestPermissions(
                activity = activity,
                rationalePanelConfig = locationService.defaultRationalePanelConfig,
                settingsPanelConfig = locationService.defaultSettingsPanelConfig,
                needsBackgroundLocation = true
            ).first()

            //then
            assertThat(result).isFalse
        }
    }

    @Test
    @DisplayName("Given no permissions has been asked, When calling getAuthorizationStatus, Then NotDetermined is returned")
    fun getAuthorizationStatusShouldReturnNotDetermined() {
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            false
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            false

        assertThat(locationService.getAuthorizationStatus()).isEqualTo(AuthorizationStatus.NotDetermined)
    }

    @Test
    @DisplayName("Given permissions has been denied, When calling getAuthorizationStatus, Then Denied is returned")
    fun getAuthorizationStatusShouldReturnDenied() {
        mockPermissionManager.alreadyRequestedPermission = setOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            false
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            false

        assertThat(locationService.getAuthorizationStatus()).isEqualTo(AuthorizationStatus.Denied)
    }

    @Test
    @DisplayName("Given only foreground permission has been authorized, When calling getAuthorizationStatus, Then AuthorizedWhenInUse is returned")
    fun getAuthorizationStatusShouldReturnAuthorizedWhenInUseOnApiQ() {
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.Q

        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_BACKGROUND_LOCATION] =
            false

        assertThat(locationService.getAuthorizationStatus()).isEqualTo(AuthorizationStatus.AuthorizedWhenInUse)
    }

    @Test
    @DisplayName("Given background permission has been authorized, When calling getAuthorizationStatus, Then AuthorizedAlways is returned")
    fun getAuthorizationStatusShouldReturnAuthorizedAlwaysOnApiQ() {
        buildConfigProvider.mockSdkInt = Build.VERSION_CODES.Q

        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] =
            true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] =
            true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_BACKGROUND_LOCATION] =
            true

        assertThat(locationService.getAuthorizationStatus()).isEqualTo(AuthorizationStatus.AuthorizedAlways)
    }

    @Test
    fun givenNoConfigRegions_currentRegions_returnsEmpty() {
        assertThat(locationService.currentRegions).isEmpty()
    }

    @Test
    fun givenConfigRegionsMatchesCurrentRegions_currentRegions_returnsWholeSet() {
        localStorage.app.core.location.currentRegions.value = setOf(1, 2)
        locationConfigurationHolder.currentConfiguration.value = LocationConfiguration(
            Accuracy.COARSE, 1, 1, listOf(
                Region(1, "", mockk(relaxed = true), 1),
                Region(2, "", mockk(relaxed = true), 1),
            )
        )
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] = true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] = true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_BACKGROUND_LOCATION] = true

        testScope.launch {
            locationService.initialize()
        }

        runTest {
            delay(500)
        }

        assertThat(locationService.currentRegions.map { it.id }.toSet()).isEqualTo(setOf(1, 2))
    }
    @Test
    fun givenConfigRegions_currentRegions_returnsSubset() {
        localStorage.app.core.location.currentRegions.value = setOf(1, 2, 3, 4)
        locationConfigurationHolder.currentConfiguration.value = LocationConfiguration(
            Accuracy.COARSE, 1, 1, listOf(
                Region(2, "", mockk(relaxed = true), 1),
                Region(3, "", mockk(relaxed = true), 1),
            )
        )
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_COARSE_LOCATION] = true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_FINE_LOCATION] = true
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.ACCESS_BACKGROUND_LOCATION] = true

        testScope.launch {
            locationService.initialize()
        }

        runTest {
            delay(500)
        }

        assertThat(locationService.currentRegions.map { it.id }.toSet()).isEqualTo(setOf(2, 3))
    }
}
