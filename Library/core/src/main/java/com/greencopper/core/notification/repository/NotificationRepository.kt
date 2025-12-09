package com.greencopper.core.notification.repository

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.content.manager.waitForContentApply
import com.greencopper.core.localstorage.*
import com.greencopper.core.networking.CoreAPI
import com.greencopper.core.networking.CoreRequest
import com.greencopper.core.networking.SignatureGenerator
import com.greencopper.core.notification.localstorage.notification
import com.greencopper.core.notification.notificationmanager.NotificationManagerClient
import com.greencopper.core.permissions.PermissionManager
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.core.secrets.SecretService
import com.greencopper.core.secrets.notificationRegistrationApi
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.d
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.Serializable

public interface NotificationRepository {
    public val token: String?
    public fun onNewToken(newToken: String)
    public suspend fun register(token: String?): RegisterResult
    public fun isRegistered(): Boolean
}

public sealed class RegisterResult {
    public object Success : RegisterResult()
    public data class Failure(val error: Throwable) : RegisterResult()
}

internal class ConcreteNotificationRepository(
    private val notificationManager: NotificationManagerClient,
    private val coreConfigurationHolder: CoreConfigurationHolder,
    private val coreAPI: CoreAPI,
    private val signatureGenerator: SignatureGenerator,
    private val secrets: SecretService,
    private val currentProjectTagProvider: CurrentProjectTagProvider,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val context: Context,
    recipeScope: CoroutineScope,
    private val backgroundScope: CoroutineScope,
    private val logging: Logging,
) : NotificationRepository, ComponentCallbacks {

    private val dispatching = Mutex()
    private var currentAppLocale = App.locale

    override val token: String?
        get() = lazyLocalStorage.resolve().app.core.notification.firebaseToken.value

    init {
        recipeScope.launch {
            coreConfigurationHolder.currentConfiguration
                .waitForContentApply(currentProjectTagProvider)
                .collectLatest { config ->
                    config?.notification?.apiUrl?.let {
                        backgroundScope.launch {
                            register(token)
                            safeUnlock()
                        }
                    }
                }
        }

        backgroundScope.launch {
            PermissionManager.currentPermissions
                .mapNotNull { it.ifEmpty { null } }
                .collectLatest {
                    register(token)
                    safeUnlock()
                }
        }

        context.registerComponentCallbacks(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (currentAppLocale != App.locale) {
            currentAppLocale = App.locale
            backgroundScope.launch {
                register(token)
            }
        }
    }

    override fun onNewToken(newToken: String) {
        lazyLocalStorage.resolve().app.core.notification.firebaseToken.value = newToken
    }

    override fun isRegistered(): Boolean =
        lazyLocalStorage.resolve().app.notificationRepository.lastRegistration.value?.pushToken?.isNotEmpty()
            ?: false

    override suspend fun register(
        token: String?,
    ): RegisterResult {
        dispatching.lock()
        val localStorage = lazyLocalStorage.resolve()
        localStorage.app.notificationRepository.currentPushToken.value = token
        val isOptIn = notificationManager.areNotificationsEnabled()

        return coreConfigurationHolder.currentConfiguration
            .filterNotNull()
            .waitForContentApply(currentProjectTagProvider).map { config ->
                val apiUrl = config.notification?.apiUrl
                val project = currentProjectTagProvider.currentProject
                    ?: run {
                        safeUnlock()
                        throw IllegalStateException("Project should be set at this point.")
                    }
                val locale = App.locale.toLanguageTag()
                val shouldUnregister = shouldUnregister(token, project, apiUrl, locale)
                if (shouldUnregister) {
                    localStorage.app.notificationRepository.lastRegistration.value?.let { registration ->
                        try {
                            val url = getRequestUrl(registration.apiUrl)
                            coreAPI.unregisterNotifications(
                                url = url,
                                authHeader = getAuthHeader(registration.project),
                            )
                        } catch (t: Throwable) {
                            logging.e(
                                message = "Failure to unregister notifications for ${registration.project}",
                                throwable = t,
                            )
                        }
                    }
                }

                if (apiUrl.isNullOrEmpty() || token.isNullOrEmpty()) {
                    localStorage.app.notificationRepository.lastRegistration.value = null
                    safeUnlock()
                    return@map RegisterResult.Success
                } else {
                    val newRegistration = Registration(apiUrl, project, token, isOptIn, locale)
                    val isNewRegistration =
                        newRegistration != localStorage.app.notificationRepository.lastRegistration.value

                    if (isNewRegistration) {
                        return@map try {
                            exponentialBackoff<RegisterResult> {
                                coreAPI.registerNotifications(
                                    url = getRequestUrl(apiUrl),
                                    authHeader = getAuthHeader(project),
                                    request = CoreRequest.Registration(
                                        registrationToken = token,
                                        platform = "android",
                                        locale = locale,
                                        isOptin = isOptIn
                                    )
                                )
                                localStorage.app.notificationRepository.lastRegistration.value =
                                    newRegistration
                                logging.d("Successfully registered to notifications for $project")
                                safeUnlock()
                                RegisterResult.Success
                            }
                        } catch (e: Throwable) {
                            logging.e(
                                message = "Failure to register notifications on $project",
                                throwable = e
                            )
                            safeUnlock()

                            RegisterResult.Failure(e)
                        }
                    }
                    safeUnlock()
                    return@map RegisterResult.Success
                }
            }.first()
    }

    private fun shouldUnregister(token: String?, project: String, apiUrl: String?, locale: String) =
        lazyLocalStorage.resolve().app.notificationRepository.lastRegistration.value?.let {
            it.pushToken != token || it.apiUrl != apiUrl || it.project != project || it.locale != locale
        } ?: false

    private fun safeUnlock() {
        try {
            dispatching.unlock()
        } catch (error: Throwable) {
            when (error) {
                //Means the mutex is already unlocked
                is IllegalStateException -> Unit
                else -> logging.e(message = "Mutex couldn't be unlocked", throwable = error)
            }
        }
    }

    private suspend fun <T> exponentialBackoff(block: suspend () -> T): T {
        var currentBackOff = 1000L
        var attempt = 0

        while (attempt < 8) {
            try {
                return block()
            } catch (e: Exception) {
                delay(currentBackOff)
                currentBackOff *= 2
                attempt++
            }
        }

        throw Exception("Maximum retries exceeded")
    }

    private fun getRequestUrl(endpointUrl: String): String =
        "$endpointUrl${lazyLocalStorage.resolve().app.installationId.value}/"

    private fun getAuthHeader(projectTag: String): String = signatureGenerator.getAuthenticationKey(
        projectTag,
        secrets.notificationRegistrationApi
    )

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
    }
}

@Serializable
internal data class Registration(
    val apiUrl: String,
    val project: String,
    val pushToken: String,
    //Set to mandatory in a few versions when most app installs will have migrated to mandatory isOptIn field
    val isOptIn: Boolean? = null,
    val locale: String? = null,
)

internal class NotificationRepositoryLocalStorageDomain(parent: AppLocalStorageDomain) :
    LocalStorageDomainBase("notificationRepository", parent) {
    val lastRegistration: LocalStorageProperty<Registration?> by localStorageProperty(null)
    val currentPushToken: LocalStorageProperty<String?> by localStorageProperty(String())
}

internal val AppLocalStorageDomain.notificationRepository: NotificationRepositoryLocalStorageDomain
    get() = NotificationRepositoryLocalStorageDomain(this)
