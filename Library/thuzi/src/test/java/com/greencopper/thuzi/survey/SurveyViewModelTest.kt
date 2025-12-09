package com.greencopper.thuzi.survey

import android.webkit.CookieManager
import android.webkit.ValueCallback
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.mocks.MockAttendeeService
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class SurveyViewModelTest {

    private val cookieManager: CookieManager = mockk(relaxed = true)

    init {
        Toolkit.setupTest()
        mockkStatic(CookieManager::class)

        every { CookieManager.getInstance() } returns cookieManager
    }

    private val localStorage: LocalStorage = App.resolve()
    private val attendeeService = MockAttendeeService()

    private val viewModel = SurveyViewModel(
        localStorage = localStorage,
        attendeeService = attendeeService
    )

    @BeforeEach
    fun beforeEach() {
        localStorage.project.thuzi.jwt.value = "testjwt"
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.now().plusDays(10).toString()
    }

    @Test
    fun jwt_injectCookie_setsCookieInManager() {
        val url = "testUrl"
        val callback = ValueCallback<Boolean> {  }
        viewModel.injectCookie(url, callback)

        verify { cookieManager.setCookie(url, any(), callback) }
    }

    @Test
    fun updateUserProfile_callsFetchAndDispatch() {
        viewModel.updateUserProfile()
        assertThat(attendeeService.fetchAndDispatchCalled).isTrue
    }
}
