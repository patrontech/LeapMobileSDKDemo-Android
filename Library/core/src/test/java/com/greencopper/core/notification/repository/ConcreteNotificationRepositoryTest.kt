package com.greencopper.core.notification.repository

import android.content.Context
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.networking.SignatureGenerator
import com.greencopper.core.notification.localstorage.notification
import com.greencopper.core.notification.notificationmanager.NotificationManagerClient
import com.greencopper.core.recipe.CoreConfiguration
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.core.secrets.SecretService
import com.greencopper.coremocks.MockCoreAPI
import com.greencopper.coremocks.MockNotificationManagerClient
import com.greencopper.coremocks.SignatureGeneratorMock
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.locale.toLocaleList
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.util.Locale

internal class ConcreteNotificationRepositoryTest : CoroutineTest(StandardTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val coreConfigurationHolder = CoreConfigurationHolder()
    private val secrets = SecretService(mapOf("notificationRegistrationApi" to "secret"))
    private val context: Context = mockk(relaxed = true)
    private val lazyLocalStorage: LazyResolver<LocalStorage> = LazyResolver.adhoc(App.resolve())

    private lateinit var coreAPI: MockCoreAPI
    private lateinit var notificationRepository: ConcreteNotificationRepository
    private lateinit var signatureGenerator: SignatureGenerator
    private lateinit var currentProjectTagProvider: CurrentProjectTagProvider
    private lateinit var notificationManager: NotificationManagerClient

    @BeforeEach
    fun beforeEach() {
        notificationManager = MockNotificationManagerClient(
            areNotificationsEnabledAction = { true },
            notifyAction = { _, _ -> },
        )
        coreAPI = MockCoreAPI()
    }

    override fun afterEach() {
        unmockkAll()
    }

    @Nested
    inner class ValidToken {

        private val token = "Token"

        private val coreConfig = CoreConfiguration(
            remoteState = CoreConfiguration.RemoteState("https://apiUrl1", 0),
            notification = CoreConfiguration.Notifications("https://apiUrl2"),
            ota = CoreConfiguration.OTA("https://apiUrl3"),
            contentConfig = CoreConfiguration.ContentConfig(
                expiration = 0,
                deprecatedProjects = listOf()
            ),
        )

        @BeforeEach
        internal fun setUp() {
            currentProjectTagProvider = object : CurrentProjectTagProvider {
                override val currentProject: String = "project"
                override val currentProjectFlow: StateFlow<String?> =
                    MutableStateFlow(currentProject)
            }
            signatureGenerator = SignatureGeneratorMock()
            notificationRepository = ConcreteNotificationRepository(
                notificationManager,
                coreConfigurationHolder,
                coreAPI,
                signatureGenerator,
                secrets,
                currentProjectTagProvider,
                lazyLocalStorage,
                context,
                testScope,
                testScope,
                MockLogging(),
            )
        }

        @Test
        fun onNewTokenShouldSucceed() {
            notificationRepository.onNewToken(token)
            assertThat(notificationRepository.token).isEqualTo("Token")
            assertThat(lazyLocalStorage.resolve().app.core.notification.firebaseToken.value).isEqualTo(
                "Token"
            )
        }

        @Test
        fun isRegisteredShouldReturnTrue() {
            lazyLocalStorage.resolve().app.notificationRepository.lastRegistration.value =
                Registration("apiUrl", "project", token, true, "en-CA")
            assertThat(notificationRepository.isRegistered()).isTrue
        }

        @Test
        fun givenEmptyToken_isRegistered_shouldReturnFalse() {
            lazyLocalStorage.resolve().app.notificationRepository.lastRegistration.value =
                Registration("apiUrl", "project", "", true, "en-CA")
            assertThat(notificationRepository.isRegistered()).isFalse
        }

        @Test
        fun givenNullToken_isRegistered_shouldReturnFalse() {
            lazyLocalStorage.resolve().app.notificationRepository.lastRegistration.value = null
            assertThat(notificationRepository.isRegistered()).isFalse
        }

        @Test
        fun register_withoutApiUrl_ShouldSucceed() {
            testScope.launch {
                val result = notificationRepository.register(token)
                assertThat(result).isEqualTo(RegisterResult.Success)
            }
        }

        @Test
        fun register_withNullOrEmptyToken_ShouldSucceed() {
            testScope.launch {
                val result = notificationRepository.register(null)
                assertThat(result).isEqualTo(RegisterResult.Success)
            }
        }

        @Test
        fun register_withPreviousRegistration_shouldUnregister_andSucceed() {
            testScope.launch {
                lazyLocalStorage.resolve().app.notificationRepository.lastRegistration.value =
                    Registration("https://apiUrl", "project", token, true)
                val result = notificationRepository.register(token)

                assertThat(coreAPI.unregisterCount).isEqualTo(1)
                assertThat(result).isEqualTo(RegisterResult.Success)
            }
        }

        @Test
        fun register_withSameRegistration_shouldDoNothing() {
            coreConfigurationHolder.currentConfiguration.value = coreConfig
            coreAPI.registerNotificationsResponse = { }
            coreAPI.unregisterNotificationsResponse = { }

            notificationRepository = ConcreteNotificationRepository(
                notificationManager,
                coreConfigurationHolder,
                coreAPI,
                signatureGenerator,
                secrets,
                currentProjectTagProvider,
                lazyLocalStorage,
                context,
                testScope,
                testScope,
                MockLogging(),
            )

            runTest(testScope.coroutineContext) {
                val registration = Registration(
                    coreConfigurationHolder.currentConfiguration.value?.notification?.apiUrl.orEmpty(),
                    currentProjectTagProvider.currentProject.orEmpty(),
                    token,
                    true,
                    App.locale.toLanguageTag()
                )
                lazyLocalStorage.resolve().app.notificationRepository.lastRegistration.value = registration
                notificationRepository.register(token)

                assertThat(coreAPI.registerCount).isEqualTo(0)
                assertThat(coreAPI.unregisterCount).isEqualTo(0)
                val value = lazyLocalStorage.resolve().app.notificationRepository.lastRegistration.value
                assertThat(value).isEqualTo(registration)
            }
        }

        @Test
        fun register_withMoreThan8Errors_shouldThrow() {

            coreConfigurationHolder.currentConfiguration.value = coreConfig
            val error = Exception("Maximum retries exceeded")
            coreAPI.registerNotificationsResponse = { throw error }

            runTest(testScope.coroutineContext) {
                val result = notificationRepository.register(token)
                assertThat(result).isInstanceOf(RegisterResult.Failure::class.java)
            }
        }

        @Test
        fun register_withConfigReady_shouldRegister() {

            coreAPI.registerNotificationsResponse = { }
            coreAPI.unregisterNotificationsResponse = { }
            coreAPI.registerCount = 0
            coreConfigurationHolder.currentConfiguration.value = coreConfig

            notificationRepository = ConcreteNotificationRepository(
                notificationManager,
                coreConfigurationHolder,
                coreAPI,
                signatureGenerator,
                secrets,
                currentProjectTagProvider,
                lazyLocalStorage,
                context,
                testScope,
                testScope,
                MockLogging(),
            )

            runTest(testScope.coroutineContext) {
                val registration = Registration(
                    coreConfigurationHolder.currentConfiguration.value?.notification?.apiUrl.orEmpty(),
                    currentProjectTagProvider.currentProject.orEmpty(),
                    token,
                    true,
                    App.locale.toLanguageTag()
                )
                val result = notificationRepository.register(token)

                assertThat(coreAPI.registerCount).isEqualTo(1)
                assertThat(result).isEqualTo(RegisterResult.Success)
                assertThat(lazyLocalStorage.resolve().app.notificationRepository.lastRegistration.value)
                    .isEqualTo(registration)
            }
        }

        @Test
        fun givenSameLocale_onConfigurationChanged_shouldNotRegister() {
            coreConfigurationHolder.currentConfiguration.value = coreConfig
            coreAPI.registerNotificationsResponse = { }
            coreAPI.unregisterNotificationsResponse = { }
            coreAPI.registerCount = 0

            notificationRepository = ConcreteNotificationRepository(
                notificationManager,
                coreConfigurationHolder,
                coreAPI,
                signatureGenerator,
                secrets,
                currentProjectTagProvider,
                lazyLocalStorage,
                context,
                testScope,
                testScope,
                MockLogging(),
            )

            runTest(testScope.coroutineContext) {
                notificationRepository.register(token)
                val registerCount = coreAPI.registerCount
                notificationRepository.onConfigurationChanged(mockk())
                assertThat(coreAPI.registerCount).isEqualTo(registerCount)
            }
        }

        @Test
        fun givenNewLocale_onConfigurationChanged_shouldRegister() {
            coreConfigurationHolder.currentConfiguration.value = coreConfig
            coreAPI.registerNotificationsResponse = { }
            coreAPI.unregisterNotificationsResponse = { }
            coreAPI.registerCount = 0

            val newConfig = context.resources.configuration
            newConfig.setLocale(Locale.TAIWAN)
            newConfig.setLocales(listOf(Locale.TAIWAN).toLocaleList())
            val newContext = context.createConfigurationContext(newConfig)

            lazyLocalStorage.resolve().app.notificationRepository.lastRegistration.value =
                Registration("https://apiUrl", "project", token, true, "oldLocale")

            notificationRepository = ConcreteNotificationRepository(
                notificationManager,
                coreConfigurationHolder,
                coreAPI,
                signatureGenerator,
                secrets,
                currentProjectTagProvider,
                lazyLocalStorage,
                newContext,
                testScope,
                testScope,
                MockLogging(),
            )

            Toolkit.setupTest(listOf(), newContext)

            runTest(testScope.coroutineContext) {
                notificationRepository.register(token)
                notificationRepository.onConfigurationChanged(mockk())
                assertThat(coreAPI.registerCount).isEqualTo(1)
            }
        }
    }

    @Nested
    inner class NoToken {
        @BeforeEach
        internal fun setUp() {
            currentProjectTagProvider = object : CurrentProjectTagProvider {
                override val currentProject: String = "project"
                override val currentProjectFlow: StateFlow<String?> =
                    MutableStateFlow(currentProject)
            }
            signatureGenerator = SignatureGeneratorMock()
            notificationRepository = ConcreteNotificationRepository(
                notificationManager,
                coreConfigurationHolder,
                coreAPI,
                signatureGenerator,
                secrets,
                currentProjectTagProvider,
                lazyLocalStorage,
                context,
                testScope,
                testScope,
                MockLogging(),
            )
        }

        @Test
        fun isRegisteredShouldReturnFalse() {
            assertThat(notificationRepository.isRegistered()).isFalse
        }

        @Test
        fun registerShouldSucceed() {
            testScope.launch {
                val result = notificationRepository.register(null)
                assertThat(result).isEqualTo(RegisterResult.Success)
            }
        }
    }

    @Nested
    inner class NoCurrentProject {
        @BeforeEach
        internal fun setUp() {
            currentProjectTagProvider = object : CurrentProjectTagProvider {
                override val currentProject: String? = null
                override val currentProjectFlow: StateFlow<String?> = MutableStateFlow(null)
            }
            signatureGenerator = SignatureGeneratorMock()
            notificationRepository = ConcreteNotificationRepository(
                notificationManager,
                coreConfigurationHolder,
                coreAPI,
                signatureGenerator,
                secrets,
                currentProjectTagProvider,
                lazyLocalStorage,
                context,
                testScope,
                testScope,
                MockLogging(),
            )
        }

        @Test
        fun registerShouldThrow() {
            testScope.launch {
                assertThrows<IllegalStateException> {
                    notificationRepository.register(null)
                }
            }
        }
    }

    @Test
    fun httpClientThrows_register_doesNotCrash() {
        coreAPI.registerNotificationsResponse = { throw Exception() }

        currentProjectTagProvider = object : CurrentProjectTagProvider {
            override val currentProject: String = "project"
            override val currentProjectFlow: StateFlow<String?> =
                MutableStateFlow(currentProject)
        }
        signatureGenerator = SignatureGeneratorMock()
        notificationRepository = ConcreteNotificationRepository(
            notificationManager,
            coreConfigurationHolder,
            coreAPI,
            signatureGenerator,
            secrets,
            currentProjectTagProvider,
            lazyLocalStorage,
            context,
            testScope,
            testScope,
            MockLogging(),
        )

        assertDoesNotThrow {
            testScope.launch {
                notificationRepository.register("token")
            }
        }
    }
}
