package com.greencopper.thuzi.account.registration

import android.webkit.CookieManager
import com.greencopper.core.content.recipe.ConfigurationHolder
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.interfacekit.MockRootLayoutManager
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.mocks.MockDeviceSessionManager
import com.greencopper.thuzi.badges.data.Badge
import com.greencopper.thuzi.localstorage.ThuziState
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.manager.ConcreteThuziRegistrationManager
import com.greencopper.thuzi.account.registration.manager.ThuziRegistrationManager
import com.greencopper.thuzi.account.registration.model.PrepareResult
import com.greencopper.thuzi.account.registration.model.RegistrationConfiguration
import com.greencopper.thuzi.account.registration.model.RegistrationData
import com.greencopper.thuzi.account.registration.model.RegistrationLayoutData
import com.greencopper.thuzi.account.registration.plugins.CustomLogoutAction
import com.greencopper.thuzi.account.registration.plugins.DefaultRegistrationProcessor
import com.greencopper.thuzi.account.registration.plugins.RegistrationPreparer
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class ConcreteThuziRegistrationManagerTest {

    private val cookieManager: CookieManager

    private val url = "https://www.example.com/"
    private val localStorage: LocalStorage
    private val rootLayoutResolver = MockRootLayoutManager()
    private val contentManager = MockContentManager(
        previousProjectsValue = { setOf() },
    )
    private val configHolder: ConfigurationHolder<RegistrationConfiguration> = mockk()
    private val remoteStateDispatcher: RemoteStateDispatcher
    private val registrationManager: ThuziRegistrationManager
    private val mockDeviceSessionManager = MockDeviceSessionManager()

    private val testLogoutAction = object : CustomLogoutAction {
        var onLogoutCalled = false
        override fun onLogout() {
            onLogoutCalled = true
        }
    }

    private val registrationData = RegistrationData(
        qrCode = "qrCode",
        attendeeId = "attendeeId",
        authToken = "jwt",
        authTokenExpiresOn = ZonedDateTime.now().plusDays(1).toString(),
        attendee = RegistrationData.Attendee("firstName")
    )

    init {
        Toolkit.setupTest()

        localStorage = App.resolve()

        remoteStateDispatcher = MockRemoteStateDispatcher(json = App.resolve())

        registrationManager = ConcreteThuziRegistrationManager(
            localStorage = localStorage,
            rootLayoutManager = rootLayoutResolver,
            registrationPreparer = object : RegistrationPreparer {
                override suspend fun prepareForRegistration(data: RegistrationLayoutData): PrepareResult =
                    PrepareResult(data.url)
            },
            registrationProcessor = DefaultRegistrationProcessor(
                localStorage,
                remoteStateDispatcher,
            ),
            customLogoutAction = testLogoutAction,
            contentManager = contentManager,
            remoteState = remoteStateDispatcher,
            deviceSessionManager = mockDeviceSessionManager,
        )

        mockkStatic(CookieManager::class)
        cookieManager = mockk<CookieManager>(relaxed = true) {
            every { removeAllCookies(any()) } returns Unit
        }
        every { CookieManager.getInstance() } returns cookieManager

        val mockConfig = mockk<RegistrationConfiguration>()
        every { mockConfig.apiUrl } returns url
        every { configHolder.currentConfiguration.value } returns mockConfig
        every { cookieManager.setCookie(any(), any()) } just Runs
        every { cookieManager.getCookie(any()) } returns "https://www.example.com/=; Path=path; Max-Age=-1"
    }

    @Test
    fun saveRegistrationData_populatesLocalStorage() {
        registrationManager.saveAndSendRegistrationData(registrationData)

        assertThat(localStorage.project.thuzi.jwtExpirationDate.value).isEqualTo(registrationData.authTokenExpiresOn)
        assertThat(localStorage.project.thuzi.jwt.value).isEqualTo(registrationData.authToken)
        assertThat(localStorage.project.thuzi.attendeeId.value).isEqualTo(registrationData.attendeeId)
        assertThat(localStorage.project.thuzi.qrCode.value).isEqualTo(registrationData.qrCode)
        assertThat(localStorage.project.thuzi.userFirstName.value).isEqualTo(registrationData.attendee?.firstName)
    }

    @Test
    fun logout_clearsLocalStorage() {
        var logoutCalled = false
        mockDeviceSessionManager.logoutImpl = { project -> logoutCalled = true }

        registrationManager.saveAndSendRegistrationData(registrationData)
        registrationManager.logout()

        assertThat(localStorage["project"].project.thuzi.jwtExpirationDate.value).isNull()
        assertThat(localStorage["project"].project.thuzi.jwt.value).isNull()
        assertThat(localStorage["project"].project.thuzi.attendeeId.value).isNull()
        assertThat(localStorage["project"].project.thuzi.qrCode.value).isNull()
        assertThat(localStorage["project"].project.thuzi.userFirstName.value).isNull()
        assertThat(localStorage["project"].project.thuzi.badges.value).isEmpty()
        assertThat(localStorage["project"].project.thuzi.state.value).isEqualTo(ThuziState())
        assertThat(logoutCalled).isTrue
    }

    @Test
    fun logout_clearLocalStorageInAllProjects() {
        // given
        contentManager.previousProjectsValue = { setOf("project2") }

        localStorage["project2"].project.thuzi.jwtExpirationDate.value = "test"
        localStorage["project2"].project.thuzi.jwt.value = "test"
        localStorage["project2"].project.thuzi.attendeeId.value = "test"
        localStorage["project2"].project.thuzi.qrCode.value = "test"
        localStorage["project2"].project.thuzi.userFirstName.value = "test"
        localStorage["project2"].project.thuzi.badges.value = listOf(Badge.UnearnedBadge("", "", "", "", "", ""))
        localStorage["project2"].project.thuzi.state.value = ThuziState(registered = true)
        localStorage["project2"].project.thuzi.registered.value = true
        localStorage["project2"].project.thuzi.config.value = RegistrationConfiguration(
            apiUrl = "https://www.example.com/",
            activationUrl = "https://www.example.com/",
            deviceLinkingUrl = "https://www.example.com/",
            userStateUpdateUrl = "https://www.example.com/",
            brandId = "brandId",
            eventId = "eventId",
            project = "project2",
            analytics = ScreenNameAnalytics("test"),
            accountDeletionApiUrl = "https://www.example.com/"
        )

        val logoutCalled: MutableMap<String, Boolean> = mutableMapOf()
        mockDeviceSessionManager.logoutImpl = { project -> logoutCalled[project] = true }

        //when
        registrationManager.saveAndSendRegistrationData(registrationData)
        registrationManager.logout()

        //then
        assertThat(localStorage["project2"].project.thuzi.jwtExpirationDate.value).isNull()
        assertThat(localStorage["project2"].project.thuzi.jwt.value).isNull()
        assertThat(localStorage["project2"].project.thuzi.attendeeId.value).isNull()
        assertThat(localStorage["project2"].project.thuzi.qrCode.value).isNull()
        assertThat(localStorage["project2"].project.thuzi.userFirstName.value).isNull()
        assertThat(localStorage["project2"].project.thuzi.badges.value).isEmpty()
        assertThat(localStorage["project2"].project.thuzi.state.value).isEqualTo(ThuziState())
        assertThat(localStorage["project2"].project.thuzi.registered.value).isFalse()
        verify(atLeast = 1) { cookieManager.removeAllCookies(any()) }

        assertThat(logoutCalled["project2"]).isTrue
    }

    @Test
    fun deleteAccount_clearsLocalStorage_updatesRootLayout() {
        mockDeviceSessionManager.logoutImpl = { _ -> }

        registrationManager.saveAndSendRegistrationData(registrationData)
        runTest {
            registrationManager.deleteAccount()
        }

        assertThat(localStorage["project"].project.thuzi.jwtExpirationDate.value).isNull()
        assertThat(localStorage["project"].project.thuzi.jwt.value).isNull()
        assertThat(localStorage["project"].project.thuzi.attendeeId.value).isNull()
        assertThat(localStorage["project"].project.thuzi.qrCode.value).isNull()
        assertThat(localStorage["project"].project.thuzi.userFirstName.value).isNull()
        assertThat(localStorage["project"].project.thuzi.badges.value).isEmpty()
        assertThat(localStorage["project"].project.thuzi.state.value).isEqualTo(ThuziState())
    }

    @Test
    fun logout_callsCustomLogoutAction() {
        mockDeviceSessionManager.logoutImpl = { _ -> }

        assertThat(testLogoutAction.onLogoutCalled).isFalse
        registrationManager.logout()
        assertThat(testLogoutAction.onLogoutCalled).isTrue
    }
}
