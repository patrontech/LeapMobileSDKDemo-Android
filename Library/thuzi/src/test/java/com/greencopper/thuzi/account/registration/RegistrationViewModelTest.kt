package com.greencopper.thuzi.account.registration

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.interfacekit.MockRootLayoutManager
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.mocks.MockDeviceSessionManager
import com.greencopper.thuzi.localstorage.ThuziState
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.mocks.MockAttendeeService
import com.greencopper.thuzi.mocks.MockCopyRoutine
import com.greencopper.thuzi.account.registration.manager.ConcreteThuziRegistrationManager
import com.greencopper.thuzi.account.registration.manager.ThuziRegistrationManager
import com.greencopper.thuzi.account.registration.model.*
import com.greencopper.thuzi.account.registration.plugins.DefaultRegistrationPreparer
import com.greencopper.thuzi.account.registration.plugins.DefaultRegistrationProcessor
import com.greencopper.thuzi.account.registration.plugins.NoOpLogoutAction
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class RegistrationViewModelTest {
    init {
        Toolkit.setupTest()
    }

    private val remoteStateDispatcher = MockRemoteStateDispatcher(json = App.resolve())
    private val localStorage: LocalStorage = App.resolve()
    private val registrationManager: ThuziRegistrationManager
    private val attendeeService = MockAttendeeService()
    private val copyRoutine = MockCopyRoutine()

    init {
        val registrationPreparer = DefaultRegistrationPreparer(
            copyRoutine = copyRoutine,
            localStorage = localStorage,
        )
        val registrationProcessor = DefaultRegistrationProcessor(
            localStorage = localStorage,
            remoteStateDispatcher = remoteStateDispatcher,
        )
        registrationManager = ConcreteThuziRegistrationManager(
            localStorage = localStorage,
            rootLayoutManager = MockRootLayoutManager(),
            registrationPreparer = registrationPreparer,
            registrationProcessor = registrationProcessor,
            contentManager = MockContentManager(),
            remoteState = remoteStateDispatcher,
            customLogoutAction = NoOpLogoutAction(),
            deviceSessionManager = MockDeviceSessionManager(),
        )
    }

    private val registrationLayoutData = RegistrationLayoutData(
        url = "url",
        activationUrl = "activationUrl",
        apiUrl = "apiUrl",
        brandId = "brandId",
        eventId = "eventId",
        onSuccessFeatureInfo = null,
        analytics = ScreenNameAnalytics("screenName"),
        onboardingPageLayoutData = OnboardingPageLayoutData("pageId"),
        redirectionHash = RedirectionHash(featureKey = FeatureKey("registration", 1)),
    )

    private val viewModel = RegistrationViewModel(
        localStorage = localStorage,
        attendeeService = attendeeService,
        registrationManager = registrationManager,
    )

    @Test
    fun saveAndSendRegistrationData_thenCompleteRegistration() {
        localStorage.project.thuzi.state.value = ThuziState()
        viewModel.saveAndSendRegistrationData(RegistrationData("", "", "", "2099-01-20T00:00:00Z"))
        assertThat(remoteStateDispatcher.dispatchedEntry).isNotNull
        viewModel.completeRegistration()
        assertThat(attendeeService.fetchAndDispatchCalled).isTrue
    }

    @Test
    fun prepareForRegistration_whenCurrentJwtIsNullAndNewTokenReceived(){
        runTest {
            copyRoutine.newToken = "newToken"
            assertThat(
                viewModel
                    .prepareForRegistration(registrationLayoutData)
            )
            .usingRecursiveComparison()
            .isEqualTo(
                PrepareResult(
                    "activationUrl",
                    "mobile=newToken; path=/; Secure;",
                )
            )
        }
    }

    @Test
    fun prepareForRegistration_whenCurrentJwtIsNullAndNewTokenReturnsNull(){
        runTest {
            copyRoutine.newToken = null
            assertThat(
                viewModel
                    .prepareForRegistration(registrationLayoutData)
            )
            .usingRecursiveComparison()
            .isEqualTo(
                PrepareResult(
                    url = "url",
                )
            )
        }
    }

    @Test
    fun prepareForRegistration_whenCurrentJwtIsNullAndNewTokenThrows(){
        runTest {
            copyRoutine.exception = RuntimeException("Fail to receive token")
            assertThat(
                viewModel
                    .prepareForRegistration(registrationLayoutData)
            )
            .usingRecursiveComparison()
            .isEqualTo(
                PrepareResult(
                    url = "url",
                )
            )
        }
    }

    @Test
    fun prepareForRegistration_whenCurrentJwtIsNotNull_shouldUseCurrent(){
        runTest {
            localStorage.project.thuzi.jwt.value = "currentJwt"
            localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.now().plusDays(10).toString()
            assertThat(
                viewModel
                    .prepareForRegistration(registrationLayoutData)
            )
            .usingRecursiveComparison()
            .isEqualTo(
                PrepareResult(
                    url = "url",
                    "mobile=currentJwt; path=/; Secure;",
                )
            )
        }
    }
}
