package com.greencopper.core.location.conditions

import com.greencopper.core.location.service.LocationService
import com.greencopper.core.permissions.AuthorizationStatus.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class LocationPermissionsConditionTest {

    private lateinit var classUnderTest: LocationPermissionsCondition
    private lateinit var locationService: LocationService

    @Nested
    @DisplayName("Given no location permission has been asked")
    inner class FirstLaunch {
        @BeforeEach
        internal fun setUp() {
            locationService = AuthorizationTestLocationService(
                authorizationStatus = NotDetermined
            )
            classUnderTest = LocationPermissionsCondition(locationService)
        }

        @Test
        @DisplayName("When checkWith is called, Then it should return false")
        fun checkWithShouldReturnFalse() {
            val result = classUnderTest.checkWith(
                LocationPermissionsCondition.LocationPermissionsConditionData("always")
            )
            assertThat(result).isFalse
        }

        @Test
        @DisplayName("When checkWithFlow is called, Then it should return false")
        fun checkWithFlowShouldReturnFalse() {
            runTest {
                val result = classUnderTest.checkWithFlow(
                    LocationPermissionsCondition.LocationPermissionsConditionData("always")
                ).first()
                assertThat(result).isFalse
            }
        }
    }

    @Nested
    @DisplayName("Given permissions have been denied")
    inner class PermissionsDenied {
        @BeforeEach
        internal fun setUp() {
            locationService = AuthorizationTestLocationService(
                authorizationStatus = Denied
            )
            classUnderTest = LocationPermissionsCondition(locationService)
        }

        @Test
        @DisplayName("When checkWith is called, Then it should return false")
        fun checkWithShouldReturnFalse() {
            val result = classUnderTest.checkWith(
                LocationPermissionsCondition.LocationPermissionsConditionData("always")
            )
            assertThat(result).isFalse
        }

        @Test
        @DisplayName("When checkWithFlow is called, Then it should return false")
        fun checkWithFlowShouldReturnFalse() {
            runTest {
                val result = classUnderTest.checkWithFlow(
                    LocationPermissionsCondition.LocationPermissionsConditionData("always")
                ).first()
                assertThat(result).isFalse
            }
        }
    }

    @Nested
    @DisplayName("Given permission whenInUse has been granted")
    inner class PermissionWhenInUseGranted {
        @BeforeEach
        internal fun setUp() {
            locationService = AuthorizationTestLocationService(
                authorizationStatus = AuthorizedWhenInUse
            )
            classUnderTest = LocationPermissionsCondition(locationService)
        }

        @Test
        @DisplayName("When checkWith is called, Then it should return false")
        fun checkWithShouldReturnFalse() {
            val result = classUnderTest.checkWith(
                LocationPermissionsCondition.LocationPermissionsConditionData("always")
            )
            assertThat(result).isFalse
        }

        @Test
        @DisplayName("When checkWithFlow is called, Then it should return false")
        fun checkWithFlowShouldReturnFalse() {
            runTest {
                val result = classUnderTest.checkWithFlow(
                    LocationPermissionsCondition.LocationPermissionsConditionData("always")
                ).first()
                assertThat(result).isFalse
            }
        }

        @Test
        @DisplayName("When checkWith is called, Then it should return true")
        fun checkWithShouldReturnTrue() {
            val result = classUnderTest.checkWith(
                LocationPermissionsCondition.LocationPermissionsConditionData("whenInUse")
            )
            assertThat(result).isTrue
        }

        @Test
        @DisplayName("When checkWithFlow is called, Then it should return true")
        fun checkWithFlowShouldReturnTrue() {
            runTest {
                val result = classUnderTest.checkWithFlow(
                    LocationPermissionsCondition.LocationPermissionsConditionData("whenInUse")
                ).first()
                assertThat(result).isTrue
            }
        }
    }

    @Nested
    @DisplayName("Given permission always has been granted")
    inner class PermissionAlwaysGranted {
        @BeforeEach
        internal fun setUp() {
            locationService = AuthorizationTestLocationService(
                authorizationStatus = AuthorizedAlways
            )
            classUnderTest = LocationPermissionsCondition(locationService)
        }

        @Test
        @DisplayName("When checkWith is called with always request, Then it should return true")
        fun checkWithShouldReturnTrueWithAlwaysRequest() {
            val result = classUnderTest.checkWith(
                LocationPermissionsCondition.LocationPermissionsConditionData("always")
            )
            assertThat(result).isTrue
        }

        @Test
        @DisplayName("When checkWithFlow is called with always request, Then it should return false")
        fun checkWithFlowShouldReturnTrueWithAlwaysRequest() {
            runTest {
                val result = classUnderTest.checkWithFlow(
                    LocationPermissionsCondition.LocationPermissionsConditionData("always")
                ).first()
                assertThat(result).isTrue
            }
        }

        @Test
        @DisplayName("When checkWith is called with whenInUse request, Then it should return true")
        fun checkWithShouldReturnTrueWithWhenInUseRequest() {
            val result = classUnderTest.checkWith(
                LocationPermissionsCondition.LocationPermissionsConditionData("whenInUse")
            )
            assertThat(result).isTrue
        }

        @Test
        @DisplayName("When checkWithFlow is called with whenInUse request, Then it should return true")
        fun checkWithFlowShouldReturnTrue() {
            runTest {
                val result = classUnderTest.checkWithFlow(
                    LocationPermissionsCondition.LocationPermissionsConditionData("whenInUse")
                ).first()
                assertThat(result).isTrue
            }
        }
    }
}