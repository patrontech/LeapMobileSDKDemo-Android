package com.greencopper.core.services.iplocation

import com.greencopper.core.content.manager.Content
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.networking.CoreAPI
import com.greencopper.coremocks.MockCoreAPI
import com.greencopper.testmocks.MockAPIProvider
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IPLocationServiceTest {

    init {
        Toolkit.setupTest()
    }

    private val ipLocation = IPLocation("NA", "CA", RestrictedArea.OUTSIDE_RESTRICTED_AREA)
    private val configurationHolder = IPLocationConfigurationHolder()
    private val localStorage = LocalStorage("project")
    val content = Content(mockk(), 1, 1, "project", OTAContent.Type.Release)
    private val contentManager = MockContentManager(
        currentContentValue = { content },
        currentContentFlowValue = { flowOf(content) }
    )
    private val coreAPI = MockCoreAPI()
    private val apiProvider = MockAPIProvider<CoreAPI>(coreAPI)

    @BeforeEach
    fun beforeEach() {
        configurationHolder.currentConfiguration.value = null
    }

    @Test
    fun serviceCompletes_withoutConfiguration_doesNotSetIPLocationInLocalStorage() = runTest {
        coreAPI.getIPLocationResponse = { ipLocation }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            val service = ConcreteIPLocationService(
                contentManager,
                apiProvider,
                localStorage,
                configurationHolder,
                this
            )
            service.completedFlow.filter { it }.collect {
                assertThat(localStorage.app.core.iplocation.value).isNull()
            }
        }
    }

    @Test
    fun serviceCompletes_withConfiguration_settingIPLocationInLocalStorage() = runTest {
        coreAPI.getIPLocationResponse = { ipLocation }
        configurationHolder.currentConfiguration.value = IPLocationConfiguration("https://endpoi.nt")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            val service = ConcreteIPLocationService(
                contentManager,
                apiProvider,
                localStorage,
                configurationHolder,
                this
            )
            service.completedFlow.filter { it }.collect {
                assertThat(localStorage.app.core.iplocation.value).isEqualTo(ipLocation)
            }
        }
    }

    @Test
    fun serviceCompletes_withExceptionInFlow_doesNotSetIPLocationInLocalStorage() = runTest {
        coreAPI.getIPLocationResponse = { throw Exception() }
        configurationHolder.currentConfiguration.value = IPLocationConfiguration("https://endpoi.nt")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            val service = ConcreteIPLocationService(
                contentManager,
                apiProvider,
                localStorage,
                configurationHolder,
                this
            )
            service.completedFlow.filter { it }.collect {
                assertThat(localStorage.app.core.iplocation.value).isNull()
            }
        }
    }

    @Test
    fun serviceCompletes_withException_doesNotSetIPLocationInLocalStorage() = runTest {
        coreAPI.getIPLocationResponse = { throw Exception() }
        configurationHolder.currentConfiguration.value = IPLocationConfiguration("https://endpoi.nt")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            val service = ConcreteIPLocationService(
                contentManager,
                apiProvider,
                localStorage,
                configurationHolder,
                this
            )
            service.completedFlow.filter { it }.collect {
                assertThat(localStorage.app.core.iplocation.value).isNull()
            }
        }
    }

    @Test
    fun serviceCompletes_ifIPLocationIsAlreadySet() = runTest {
        localStorage.app.core.iplocation.value = ipLocation
        coreAPI.getIPLocationResponse = { throw Exception() }
        configurationHolder.currentConfiguration.value = IPLocationConfiguration("https://endpoi.nt")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            val service = ConcreteIPLocationService(
                contentManager,
                apiProvider,
                localStorage,
                configurationHolder,
                this
            )
            service.completedFlow.filter { it }.collect {
                assertThat(localStorage.app.core.iplocation.value).isEqualTo(ipLocation)
            }
        }
    }
}
