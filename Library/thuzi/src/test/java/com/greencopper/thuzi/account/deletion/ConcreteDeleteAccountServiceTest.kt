package com.greencopper.thuzi.account.deletion

import com.greencopper.core.localstorage.Email
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.user
import com.greencopper.core.secrets.SecretService
import com.greencopper.coremocks.SignatureGeneratorMock
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.mocks.MockThuziAPI
import com.greencopper.thuzi.mocks.MockThuziRegistrationManager
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.unmockkAll
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Locale

internal class ConcreteDeleteAccountServiceTest : CoroutineTest(UnconfinedTestDispatcher()) {
    init {
        Toolkit.setupTest()
    }

    private val localStorage: LocalStorage = App.resolve()
    private val registrationManager = MockThuziRegistrationManager()
    private val secretsMap = mutableMapOf<String, String>()
    private val mockLogging = MockLogging()

    private val thuziAPI = MockThuziAPI(deleteAccountResponse = { })

    private val classUnderTest = ConcreteDeleteAccountService(
        SecretService(secretsMap),
        SignatureGeneratorMock(),
        registrationManager,
        thuziAPI,
        localStorage,
        mockLogging,
        Locale.CANADA,
    )

    override fun afterEach() {
        unmockkAll()
    }

    @Test
    fun whenSecretNotFound_shouldReturnFail() = runTest {
        val result = classUnderTest.deleteAccount("")
        assertThat(result).isEqualTo(DeleteAccountResult.FAIL)
        assertThat(mockLogging.lastThrowable?.message)
            .isEqualTo("accountDeletionApi secret not found")
        assertThat(registrationManager.logoutCalled).isFalse
    }

    @Test
    fun whenEmailNotFound_shouldReturnFail() = runTest {
        secretsMap["accountDeletionApi"] = "testSecret"
        val result = classUnderTest.deleteAccount("")
        assertThat(result).isEqualTo(DeleteAccountResult.FAIL)
        assertThat(mockLogging.lastThrowable?.message)
            .isEqualTo("thuzi email not found")
        assertThat(registrationManager.logoutCalled).isFalse
    }

    @Test
    fun whenAttendeeIdNotFound_shouldReturnFail() = runTest {
        secretsMap["accountDeletionApi"] = "testSecret"
        localStorage.project.user.putEmail(Email.THUZI, "foo@bar.com")
        val result = classUnderTest.deleteAccount("")
        assertThat(result).isEqualTo(DeleteAccountResult.FAIL)
        assertThat(mockLogging.lastThrowable?.message)
            .isEqualTo("thuzi attendee id not found")
        assertThat(registrationManager.logoutCalled).isFalse
    }

    @Test
    fun whenDoingRequest_whenRequestSuccessful_shouldReturnSuccess() = runTest {
        secretsMap["accountDeletionApi"] = "testSecret"
        localStorage.project.user.putEmail(Email.THUZI, "test@mail.com")
        localStorage.project.thuzi.jwt.value = "jwt"
        localStorage.project.thuzi.jwtExpirationDate.value = "2100-01-01T00:00:00Z"
        localStorage.project.thuzi.attendeeId.value = "attendee"
        val result = classUnderTest.deleteAccount("")
        assertThat(result).isEqualTo(DeleteAccountResult.SUCCESS)
        assertThat(registrationManager.logoutCalled).isTrue
    }

    @Test
    fun whenDoingRequest_whenRequestFailed_shouldReturnFail() = runTest {
        thuziAPI.deleteAccountResponse = { throw Exception() }
        secretsMap["accountDeletionApi"] = "testSecret"
        localStorage.project.user.putEmail(Email.THUZI, "test@mail.com")
        val result = classUnderTest.deleteAccount("")
        assertThat(result).isEqualTo(DeleteAccountResult.FAIL)
        assertThat(registrationManager.logoutCalled).isFalse
    }
}
