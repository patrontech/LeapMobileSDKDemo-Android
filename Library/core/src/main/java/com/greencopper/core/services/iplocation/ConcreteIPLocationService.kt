package com.greencopper.core.services.iplocation

import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.networking.CoreAPI
import com.greencopper.toolkit.App
import com.greencopper.toolkit.httpclient.APIProvider
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.Duration

internal class ConcreteIPLocationService(
    contentManager: ContentManager,
    coreAPIProvider: APIProvider<CoreAPI>,
    localStorage: LocalStorage,
    configurationHolder: IPLocationConfigurationHolder,
    scope: CoroutineScope,
): IPLocationService {

    internal companion object {
        val API_TIMEOUT: Duration = Duration.ofSeconds(3)
    }

    private val _completedFlow = MutableStateFlow(false)

    override val completedFlow: Flow<Boolean>
        get() = _completedFlow

    private val coreAPI: CoreAPI = coreAPIProvider.api(API_TIMEOUT)

    init {
        if (localStorage.app.core.iplocation.value != null) {
            // We only ask for IPLocation information once.
            // Once we have it, we don't try to fetch it again, even after a config change.
            _completedFlow.value = true
        } else {
            scope.launch {
                // IPLocationService config is optional.
                // Run only once after the first content has been applied, and then set to completed.
                contentManager
                    .currentContentFlow
                    .filterNotNull()
                    .flatMapLatest {
                        _completedFlow
                    }
                    .filter { completed ->
                        // If already completed, we don't want to run the flow again
                        !completed
                    }
                    .flatMapLatest {
                        configurationHolder.currentConfiguration
                    }.collect { ipConfig ->
                        val ipLocation = ipConfig?.let { fetchIPLocation(ipConfig) }
                        localStorage.app.core.iplocation.value = ipLocation
                        _completedFlow.value = true
                    }
            }
        }
    }

    private suspend fun fetchIPLocation(configuration: IPLocationConfiguration): IPLocation? {
        return try {
            coreAPI.getIPLocation(configuration.endpoint)
        } catch (t: Throwable) {
            App.log.e(
                "Failed to fetch IPLocation from ${configuration.endpoint}.",
                tag = "IPLocationService",
                throwable = t
            )
            null
        }
    }
}
