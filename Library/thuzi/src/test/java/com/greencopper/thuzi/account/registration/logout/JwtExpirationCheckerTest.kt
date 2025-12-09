package com.greencopper.thuzi.account.registration.logout

import android.webkit.CookieManager
import com.greencopper.core.content.recipe.ConfigurationHolder
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.interfacekit.MockRootLayoutManager
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.mocks.MockDeviceSessionManager
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.manager.ConcreteThuziRegistrationManager
import com.greencopper.thuzi.account.registration.manager.logout.JwtExpirationChecker
import com.greencopper.thuzi.account.registration.model.RegistrationConfiguration
import com.greencopper.thuzi.account.registration.plugins.NoOpLogoutAction
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.*

internal class JwtExpirationCheckerTest {

    init {
        Toolkit.setupTest()
    }

    private val rootLayoutManager = MockRootLayoutManager()
    private val localStorage: LocalStorage = App.resolve()
    private val mockContentManager = MockContentManager()
    private val mockDeviceSessionManager = MockDeviceSessionManager()

    private val registrationManager = ConcreteThuziRegistrationManager(
        localStorage = localStorage,
        rootLayoutManager = rootLayoutManager,
        registrationPreparer = mockk(),
        registrationProcessor = mockk(),
        customLogoutAction = NoOpLogoutAction(),
        contentManager = mockContentManager,
        remoteState = MockRemoteStateDispatcher(json = App.resolve()),
        deviceSessionManager = mockDeviceSessionManager,
    )

    @Test
    fun testExpired() =
        runTest {
            mockContentManager.previousProjectsValue = { setOf() }
            mockDeviceSessionManager.logoutImpl = { _ -> }

            mockkStatic(CookieManager::class)
            val cookieManager = mockk<CookieManager>(relaxed = true) {
                every { removeAllCookies(any()) } returns Unit
            }
            every { CookieManager.getInstance() } returns cookieManager

            val mockConfig = mockk<RegistrationConfiguration>()
            every { mockConfig.apiUrl } returns "http://www.example.com"
            val configHolder: ConfigurationHolder<RegistrationConfiguration> = mockk()
            every { configHolder.currentConfiguration.value } returns mockConfig

            localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.of(
                LocalDateTime.now().plusDays(-1),
                ZoneId.systemDefault(),
            ).toString()
            val checker = JwtExpirationChecker(registrationManager, localStorage, rootLayoutManager)
            checker.checkExpiration()
            assertThat(rootLayoutManager.updateRootLayoutCalled).isTrue
            assertThat(localStorage.project.thuzi.jwtExpirationDate.value).isNull()
        }

    @Test
    fun testNotSignedIn() =
        runTest {
            val checker = JwtExpirationChecker(registrationManager, localStorage, rootLayoutManager)
            checker.checkExpiration()
            assertThat(rootLayoutManager.updateRootLayoutCalled).isFalse
        }

    @Test
    fun testSignedIn() =
        runTest {
            localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.of(
                LocalDateTime.now().plusDays(1),
                ZoneId.systemDefault(),
            ).toString()
            localStorage.project.thuzi.jwt.value = "abc123"
            val checker = JwtExpirationChecker(registrationManager, localStorage, rootLayoutManager)
            checker.checkExpiration()
            assertThat(rootLayoutManager.updateRootLayoutCalled).isFalse
        }
}
