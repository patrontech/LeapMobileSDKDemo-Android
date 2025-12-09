package com.greencopper.thuzi.fanscan

import com.greencopper.core.deferredcommand.DeferredCommandKey
import com.greencopper.core.deferredcommand.DeferredCommandState
import com.greencopper.core.localstorage.*
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.thuzi.mocks.MockDeviceSessionManager
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.mocks.MockThuziAPI
import com.greencopper.thuzi.models.DeviceSession
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.util.UUID

internal class FanscanDeferredCommandTest {

    private val aggregateMetricsService = MockAggregateMetricsService()
    private val container = TestLocalStorageContainer()
    private val thuziAPI = MockThuziAPI(checkInResponse = { })
    private val serverUrl = "https://checkin/"
    private val moduleId = "xyz"
    private val project = "test"
    private val checkIn = FanscanCheckIn(serverUrl, moduleId, project)
    private val localStorage = LocalStorage(project, container)

    private val command = FanscanDeferredCommand(
        thuziAPI = thuziAPI,
        localStorage = localStorage,
        deviceSessionManager = MockDeviceSessionManager(
            getDeviceSessionImpl = { project -> DeviceSession(installationId = UUID.randomUUID().toString()) }
        ),
        logger = MockLogging(),
    )

    init {
        Toolkit.setupTest()
        bindSingleton(aggregateMetricsService as AggregateMetricsService)
    }

    @Test
    fun whenCommandStateDataIsEmpty_commandIsCancelled() {
        val state = DeferredCommandState(DeferredCommandKey.fanscan)
        runTest {
            assertThat(command.execute(state)).isEmpty()
        }
    }

    @Test
    fun whenCommandStateDataIsInvalid_commandIsCancelled() {
        val state = DeferredCommandState.create(DeferredCommandKey.fanscan, 99)
        runTest {
            assertThat(command.execute(state)).isEmpty()
        }
    }

    @Test
    fun whenCommandStateIsValidWithoutJWT_commandIsRescheduled() {
        val state = DeferredCommandState.create(DeferredCommandKey.fanscan, checkIn)
        runTest {
            assertThat(command.execute(state)).isNotEmpty
        }
    }

    @Test
    fun whenCommandStateIsValidWithJWT_commandIsExecuted() {
        val state = DeferredCommandState.create(DeferredCommandKey.fanscan, checkIn)
        localStorage.project.thuzi.jwtExpirationDate.value = "3000-12-31T00:00Z"
        localStorage.project.thuzi.jwt.value = "jwt"
        runTest {
            assertThat(command.execute(state)).isEmpty()
        }
        assertThat(thuziAPI.checkInCount).isEqualTo(1)
        assertThat(
            aggregateMetricsService.wasMetricTracked(FanscanApiCaller.CheckInSuccessEvent::class)
        ).isTrue
    }

    @Test
    fun whenHTTP404_commandIsCancelled() {
        thuziAPI.checkInResponse = { throw HttpException(Response.error<String>(404, "".toResponseBody())) }

        val state = DeferredCommandState.create(DeferredCommandKey.fanscan, checkIn)
        localStorage.project.thuzi.jwtExpirationDate.value = "3000-12-31T00:00Z"
        localStorage.project.thuzi.jwt.value = "jwt"
        runTest {
            assertThat(command.execute(state)).isEmpty()
        }
        assertThat(
            aggregateMetricsService.wasMetricTracked(FanscanApiCaller.CheckInFailureEvent::class)
        ).isTrue
    }

    @Test
    fun whenHTTP401_commandIsReschuledAndUserIsSignedOut() {
        thuziAPI.checkInResponse = { throw HttpException(Response.error<String>(401, "".toResponseBody())) }

        val state = DeferredCommandState.create(DeferredCommandKey.fanscan, checkIn)
        localStorage.project.thuzi.jwtExpirationDate.value = "3000-12-31T00:00Z"
        localStorage.project.thuzi.jwt.value = "jwt"
        runTest {
            assertThat(command.execute(state)).isNotEmpty
        }
        assertThat(localStorage.project.thuzi.jwt.value).isNull()
        assertThat(
            aggregateMetricsService.wasMetricTracked(FanscanApiCaller.CheckInFailureEvent::class)
        ).isTrue
    }

    @Test
    fun whenHTTP403_commandIsRescheduledAndUserIsSignedOut() {
        thuziAPI.checkInResponse = { throw HttpException(Response.error<String>(403, "".toResponseBody())) }

        val state = DeferredCommandState.create(DeferredCommandKey.fanscan, checkIn)
        localStorage.project.thuzi.jwtExpirationDate.value = "3000-12-31T00:00Z"
        localStorage.project.thuzi.jwt.value = "jwt"
        runTest {
            assertThat(command.execute(state)).isNotEmpty()
        }
        assertThat(localStorage.project.thuzi.jwt.value).isNull()
        assertThat(
            aggregateMetricsService.wasMetricTracked(FanscanApiCaller.CheckInFailureEvent::class)
        ).isTrue
    }

    @Test
    fun whenUnexpectedHTTPResponseIsReceived_commandIsRescheduled() {
        thuziAPI.checkInResponse = { throw HttpException(Response.error<String>(500, "".toResponseBody())) }

        val state = DeferredCommandState.create(DeferredCommandKey.fanscan, checkIn)
        localStorage.project.thuzi.jwtExpirationDate.value = "3000-12-31T00:00Z"
        localStorage.project.thuzi.jwt.value = "jwt"
        runTest {
            val nextStates = command.execute(state)
            assertThat(nextStates).isNotEmpty
            val nextCheckIn = nextStates.first().get<FanscanCheckIn>()!!
            assertThat(nextCheckIn.attempts).isEqualTo(1)
        }
        assertThat(
            aggregateMetricsService.wasMetricTracked(FanscanApiCaller.CheckInFailureEvent::class)
        ).isTrue
    }

    @Test
    fun whenUnexpectedHTTPResponseIsReceived_commandIsCancelledAfterMaximumAttempts() {
        thuziAPI.checkInResponse = { throw HttpException(Response.error<String>(500, "".toResponseBody())) }

        var state = DeferredCommandState.create(DeferredCommandKey.fanscan, checkIn)
        localStorage.project.thuzi.jwtExpirationDate.value = "3000-12-31T00:00Z"
        localStorage.project.thuzi.jwt.value = "jwt"
        runTest {
            for (i in 0 until (FanscanDeferredCommand.MAXIMUM_ATTEMPTS - 1)) {
                state = command.execute(state).first()
            }
            assertThat(command.execute(state)).isEmpty()
        }
        assertThat(
            aggregateMetricsService.wasMetricTracked(FanscanApiCaller.CheckInFailureEvent::class)
        ).isTrue
    }
}
