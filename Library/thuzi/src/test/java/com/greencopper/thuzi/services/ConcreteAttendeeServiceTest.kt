package com.greencopper.thuzi.services

import android.util.MalformedJsonException
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.TestLocalStorageContainer
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.core.MockCurrentProjectTagProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.thuzi.ThuziResponse
import com.greencopper.thuzi.localstorage.ThuziState
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.mocks.MockThuziAPI
import com.greencopper.thuzi.mocks.MockThuziRegistrationManager
import com.greencopper.thuzi.account.registration.model.RegistrationConfiguration
import com.greencopper.thuzi.account.registration.recipe.RegistrationConfigurationHolder
import com.greencopper.thuzi.services.attendee.ConcreteAttendeeService
import com.greencopper.thuzi.services.attendee.VirtualAccessCard
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import com.greencopper.thuzi.localstorage.Attendee as LocalStorageAttendee

internal class ConcreteAttendeeServiceTest : CoroutineTest(StandardTestDispatcher()) {

    private val testLocalStorage: LazyResolver<LocalStorage>
    private val registrationConfigurationHolder = RegistrationConfigurationHolder()
    private val contentManager = MockContentManager(
        previousProjectsValue = { setOf() },
    )

    private val jwt = "jwt"

    private val currentProjectTagProvider = MockCurrentProjectTagProvider(currentProjectFlowImpl = { flowOf("project") })
    private val registrationManager = MockThuziRegistrationManager()
    private val config = RegistrationConfiguration(
        "https://api.events.thuzi.com/api",
        "https://api.events.thuzi.com/api",
        "https://api.events.thuzi.com/api",
        "https://api.events.thuzi.com/api",
        "brand",
        "event",
        "project",
        ScreenNameAnalytics("test"),
        "accountDeletionApiUrl",
    )

    private val thuziAPI = MockThuziAPI(
        getAttendeeResponse = {
            ThuziResponse.Attendee(
                email = "email@gmail.com",
                postalCode = "A1A 1A1",
                customAnswers = listOf("0", "1"),
            )
        },
        getProfileResponse = {
            mapOf("testBoolean" to "true")
        }
    )

    private lateinit var attendeeService: ConcreteAttendeeService

    init {
        Toolkit.setupTest()
        bindProvider(LocalStorage("project", TestLocalStorageContainer()))

        testLocalStorage = LazyResolver.adhoc(App.resolve())

        val date = LocalDateTime.now().plusYears(1)
        testLocalStorage.resolve().project.thuzi.jwtExpirationDate.value =
            ZonedDateTime.of(date, ZoneId.systemDefault()).toString()
        testLocalStorage.resolve().project.thuzi.qrCode.value = "qrCode"
        testLocalStorage.resolve().project.thuzi.jwt.value = jwt

        registrationConfigurationHolder.currentConfiguration.value = config
    }

    override fun afterEach() {}

    private fun initAttendeeService() {
        attendeeService = ConcreteAttendeeService(
            lazyLocalStorage = testLocalStorage,
            registrationManager = registrationManager,
            remoteStateDispatcher = MockRemoteStateDispatcher(json = App.resolve()),
            thuziAPI = thuziAPI,
            registrationConfigurationHolder = registrationConfigurationHolder,
            currentProjectTagProvider = currentProjectTagProvider,
            scope = testScope,
            contentManager = contentManager,
            logging = MockLogging()
        )
    }

    @Test
    fun checkAttendeeService_shouldSucceed() {
        runTest {
            initAttendeeService()

            delay(1500)

            assertThat(thuziAPI.getAttendeeCount).isEqualTo(1)
            assertThat(thuziAPI.getProfileCount).isEqualTo(1)
            val thuziState = testLocalStorage.resolve().project.thuzi.state.value
            assertThat(thuziState.attendee.postalCode).isEqualTo("A1A 1A1")
            assertThat(thuziState.answers["testBoolean"]).isEqualTo("true")
            assertThat(thuziState.answers["0"]).isEqualTo("0")
        }
    }

    @Test
    fun checkAttendeeService_shouldSucceed_withVAC() {
        thuziAPI.getAttendeeResponse = {
            ThuziResponse.Attendee(
                email = "email@gmail.com",
                postalCode = "A1A 1A1",
                customAnswers = listOf("0", "1"),
                virtualAccessCards = listOf(VirtualAccessCard("id"), VirtualAccessCard("id"))
            )
        }

        runTest {
            initAttendeeService()

            delay(1500)

            assertThat(thuziAPI.getAttendeeCount).isEqualTo(1)
            assertThat(thuziAPI.getProfileCount).isEqualTo(1)
            val thuziState = testLocalStorage.resolve().project.thuzi.state.value
            assertThat(thuziState.attendee.postalCode).isEqualTo("A1A 1A1")
            assertThat(thuziState.answers["testBoolean"]).isEqualTo("true")
            assertThat(thuziState.answers["0"]).isEqualTo("0")
            assertThat(thuziState.virtualAccessCards?.first()).isEqualTo("id")
        }
    }

    @Test
    fun checkAttendeeService_whenWrongData() {
        assertDoesNotThrow {
            runTest {
                thuziAPI.getAttendeeResponse = { throw MalformedJsonException("") }
                thuziAPI.getProfileResponse = { throw MalformedJsonException("") }

                initAttendeeService()

                delay(1500)

                assertThat(thuziAPI.getAttendeeCount).isEqualTo(1)
                val thuziState = testLocalStorage.resolve().project.thuzi.state.value
                assertThat(thuziState.attendee.postalCode).isNull()
                assertThat(thuziState.answers).isEmpty()
            }
        }
    }

    @Test
    fun checkAttendeeService_whenHttpException_Unauthorized() {
        runTest {
            thuziAPI.getAttendeeResponse =
                { throw HttpException(Response.error<String>(HttpURLConnection.HTTP_UNAUTHORIZED, "".toResponseBody())) }

            initAttendeeService()

            delay(1500)
            assertThat(registrationManager.logoutCalled).isTrue
        }
    }

    @Test
    fun checkAttendeeService_whenHttpException_Forbidden() {
        runTest {
            thuziAPI.getAttendeeResponse = { throw HttpException(Response.error<String>(HttpURLConnection.HTTP_FORBIDDEN, "".toResponseBody())) }

            initAttendeeService()

            delay(1500)
            assertThat(registrationManager.logoutCalled).isTrue
        }
    }

    @Test
    fun checkAttendeeService_whenHttpException_OtherHttpCode() {
        runTest {
            thuziAPI.getAttendeeResponse = { throw HttpException(Response.error<String>(HttpURLConnection.HTTP_BAD_GATEWAY, "".toResponseBody())) }

            initAttendeeService()

            delay(1500)
            val storedJwt = testLocalStorage.resolve().project.thuzi.jwt.value
            assertThat(storedJwt).isEqualTo(jwt)
        }
    }

    @Test
    fun checkAttendeeService_whenOtherException() {
        runTest {
            thuziAPI.getAttendeeResponse = { throw Exception() }

            initAttendeeService()

            delay(1500)
            val storedJwt = testLocalStorage.resolve().project.thuzi.jwt.value
            assertThat(storedJwt).isEqualTo(jwt)
        }
    }

    @Test
    fun checkAttendeeService_whenJwtNull() {
        testLocalStorage.resolve().project.thuzi.jwt.value = null

        runTest {
            initAttendeeService()
            delay(1500)
            val state = testLocalStorage.resolve().project.thuzi.state.value
            assertThat(state).isEqualTo(ThuziState())
        }
    }

    @Test
    fun checkAttendeeService_whenQrCodeNull() {
        testLocalStorage.resolve().project.thuzi.qrCode.value = null

        runTest {
            initAttendeeService()
            delay(1500)
            val state = testLocalStorage.resolve().project.thuzi.state.value
            assertThat(state).isEqualTo(ThuziState())
        }
    }

    @Test
    fun checkAttendeeService_whenConfigNull() {
        registrationConfigurationHolder.currentConfiguration.value = null

        runTest {
            initAttendeeService()
            delay(1500)
            val state = testLocalStorage.resolve().project.thuzi.state.value
            assertThat(state).isEqualTo(ThuziState())
        }
    }

    @Test
    fun checkAttendeeService_whenPreviousThuziStateExists() {
        thuziAPI.getAttendeeResponse = { throw Exception() }

        contentManager.previousProjectsValue = { setOf("project") }

        val thuziState = ThuziState(
            answers = mapOf("1" to "2"),
            attendee = LocalStorageAttendee("postalCode"),
            virtualAccessCards = listOf("VirtualCard")
        )

        testLocalStorage.resolve()["project"].project.thuzi.state.value = thuziState

        runTest {
            initAttendeeService()
            attendeeService.fetchAndDispatch()
            delay(1500)

            val thuziStateApplied = testLocalStorage.resolve().project.thuzi.state.value
            assertThat(thuziStateApplied).isEqualTo(thuziState)
        }
    }
}
