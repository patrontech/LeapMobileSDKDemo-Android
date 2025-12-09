package com.greencopper.thuzi.fanscan

import android.Manifest
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.labels.*
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.testmocks.core.*
import com.greencopper.testmocks.interfacekit.MockLinkResolver
import com.greencopper.testmocks.interfacekit.MockRouteController
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.thuzi.mocks.MockDeviceSessionManager
import com.greencopper.thuzi.metrics.fanScanCheckInFailure
import com.greencopper.thuzi.metrics.fanScanCheckInSuccess
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.mocks.MockThuziAPI
import com.greencopper.thuzi.mocks.MockThuziRegistrationManager
import com.greencopper.thuzi.models.DeviceSession
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection
import java.time.ZonedDateTime
import java.util.UUID

internal class FanscanViewModelTest {

    init {
        Toolkit.setupTest()
    }

    private val thuziAPI = MockThuziAPI(
        checkInResponse = { }
    )
    private val mockPermissionManager = MockPermissionManager()
    private val mockRegistrationManager = MockThuziRegistrationManager()
    private val localStorage: LocalStorage = App.resolve()
    private val mockLinkResolver = MockLinkResolver()
    private val mockRouteController = MockRouteController()
    private val mockDeferredCommandService = MockDeferredCommandService()

    private val mappedProvider = MockingMappedProvider()

    private val viewModel = FanscanViewModel(
        thuziAPI = thuziAPI,
        permissionManager = mockPermissionManager,
        localizationService = MockLocalizationService(),
        registrationManager = mockRegistrationManager,
        localStorage = localStorage,
        linkResolver = mockLinkResolver,
        routeController = mockRouteController,
        deferredCommandService = mockDeferredCommandService,
        deviceSessionManager = MockDeviceSessionManager(
            getDeviceSessionImpl = { _ -> DeviceSession(installationId = UUID.randomUUID().toString())
        }),
        currentProjectTagProvider = MockCurrentProjectTagProvider(currentProjectImpl = { "project" }),
        logging = MockLogging(),
    )

    @BeforeEach
    fun beforeEach() {
        localStorage.project.thuzi.jwt.value = "jwt"
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.now().plusDays(10).toStr()
        mappedProvider.enable()
    }

    @Test
    fun checkin_addsParams_returnsValue() {
        val moduleId = "moduleId"
        val checkinUrl = "https://checkinUrl.com/"
        val qrCode = "qrCode"
        localStorage.project.thuzi.qrCode.value = qrCode

        runTest {
            viewModel.checkIn(moduleId, checkinUrl)
            assertThat(thuziAPI.checkInCount).isEqualTo(1)
        }
    }

    @Test
    fun cameraPermissionGranted_hasCameraPermission_returnsTrue() {
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.CAMERA] = true
        assertThat(viewModel.hasCameraPermission()).isTrue
    }

    @Test
    fun cameraPermissionDenied_hasCameraPermission_returnsFalse() {
        mockPermissionManager.alreadyGrantedPermissionsMockMap[Manifest.permission.CAMERA] = false
        assertThat(viewModel.hasCameraPermission()).isFalse
    }

    @Test
    fun withActivity_requestCameraPermissions_permissionRequested() {
        viewModel.requestCameraPermission(mockk())
        assertThat(mockPermissionManager.requestedPermissions.contains(Manifest.permission.CAMERA)).isTrue
    }

    @Test
    fun withNoActivity_requestCameraPermissions_returnsFalse() {
        runTest {
            val result = viewModel.requestCameraPermission(null).first()
            assertThat(result).isFalse
        }
    }

    @Test
    fun requestCodeForbidden_checkTokenExpiration_callsLogout() {
        val exception = HttpException(Response.error<String>(HttpURLConnection.HTTP_FORBIDDEN, "".toResponseBody()))
        viewModel.checkTokenExpiration(exception)
        assertThat(mockRegistrationManager.logoutCalled).isTrue
    }

    @Test
    fun requestCodeUnauthorized_checkTokenExpiration_callsLogout() {
        val exception = HttpException(Response.error<String>(HttpURLConnection.HTTP_UNAUTHORIZED, "".toResponseBody()))
        viewModel.checkTokenExpiration(exception)
        assertThat(mockRegistrationManager.logoutCalled).isTrue
    }

    @Test
    fun requestCodeBadRequest_checkTokenExpiration_doesNotCallLogout() {
        val exception = HttpException(Response.error<String>(HttpURLConnection.HTTP_BAD_GATEWAY, "".toResponseBody()))
        viewModel.checkTokenExpiration(exception)
        assertThat(mockRegistrationManager.logoutCalled).isFalse
    }

    @Test
    fun otherException_checkTokenExpiration_doesNotCallLogout() {
        viewModel.checkTokenExpiration(Exception())
        assertThat(mockRegistrationManager.logoutCalled).isFalse
    }

    @Test
    fun resolvableRoute_handleRedirectionUrl_callsResolve() {
        val url = "route1"
        val route = Route.Present(mockk())
        mockLinkResolver.mockRoutes = mapOf(url to route)

        viewModel.handleRedirectionUrl(url, mockk())

        assertThat(mockRouteController.lastResolveRoute).isEqualTo(route)
    }

    @Test
    fun unresolvableRoute_handleRedirectionUrl_doesNotCallResolve() {
        viewModel.handleRedirectionUrl("testUrl", mockk())
        assertThat(mockRouteController.lastResolveRoute).isNull()
    }

    @Test
    fun trackCheckinSuccess() {
        val id = "successId"
        val event = FanscanApiCaller.CheckInSuccessEvent(id)
        event.track(mappedProvider)
        assertThat(
            mappedProvider.wasMetricTracked(
                EventName.fanScanCheckInSuccess, mapOf(EventParameter.itemId to id)
            )
        ).isTrue
    }

    @Test
    fun trackCheckinFailure() {
        val id = "failureId"
        val event = FanscanApiCaller.CheckInFailureEvent(id)
        event.track(mappedProvider)
        assertThat(
            mappedProvider.wasMetricTracked(
                EventName.fanScanCheckInFailure, mapOf(EventParameter.itemId to id)
            )
        ).isTrue
    }
}
