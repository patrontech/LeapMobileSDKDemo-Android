package com.greencopper.core.remotestate

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.networking.SignatureGenerator
import com.greencopper.core.recipe.CoreConfiguration
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.coremocks.MockCoreAPI
import com.greencopper.coremocks.SignatureGeneratorMock
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.clearAllMocks
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ProjectRemoteStateDispatcherTest : CoroutineTest(UnconfinedTestDispatcher()) {
    private val localStorage: LocalStorage
    private val coreConfigHolder: CoreConfigurationHolder
    private val coreAPI = MockCoreAPI(sendUserStateResponse = { })
    private val signatureGenerator: SignatureGenerator = SignatureGeneratorMock()
    private var remoteStateConfig =
        CoreConfiguration.RemoteState("https://apiUrl.com", threshold = 10)

    init {
        Toolkit.setupTest()
        localStorage = App.resolve()
        coreConfigHolder = CoreConfigurationHolder()
        bindSingleton(coreConfigHolder)

        coreConfigHolder.currentConfiguration.value = null
    }

    override fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `When dispatching one non-urgent value, don't dispatch it`() {
        val remoteStateDispatcher = createRemoteStateDispatcher(remoteStateConfig)

        remoteStateDispatcher.dispatch("testKey", JsonPrimitive("testValue"), isUrgent = false, domain = RemoteStateEntry.Domain.PROJECT)
        assertThat(coreAPI.sendUserStateCount).isEqualTo(0)
    }

    @Test
    fun `When dispatching one urgent value, dispatch it`() {
        val remoteStateDispatcher = createRemoteStateDispatcher(remoteStateConfig)

        remoteStateDispatcher.dispatch("testKey", JsonPrimitive("testValue"), isUrgent = true, domain = RemoteStateEntry.Domain.PROJECT)
        runTest {
            delay(2000)
            assertThat(coreAPI.sendUserStateCount).isEqualTo(1)
        }
    }

    @Test
    fun `When dispatching non-urgent values strictly above threshold, dispatch them`() {
        val threshold = 4

        val remoteStateDispatcher = createRemoteStateDispatcher(
            CoreConfiguration.RemoteState(
                "https://apiUrl.com",
                threshold = threshold
            )
        )

        repeat(threshold + 1) {
            remoteStateDispatcher.dispatch("testKey", JsonPrimitive("testValue"), isUrgent = false, domain = RemoteStateEntry.Domain.PROJECT)
        }

        runTest {
            delay(2000)
            assertThat(coreAPI.sendUserStateCount).isEqualTo(1)
        }
    }

    @Test
    fun `When recreating dispatcher messages queue should be the same`() {
        val remoteStateDispatcher = createRemoteStateDispatcher(remoteStateConfig)
        assertThat(remoteStateDispatcher.messagesQueue).hasSize(0)

        remoteStateDispatcher.dispatch("testKey", JsonPrimitive("testValue"), isUrgent = false, domain = RemoteStateEntry.Domain.PROJECT)
        assertThat(remoteStateDispatcher.messagesQueue).hasSize(1)

        val newStateDispatcher = createRemoteStateDispatcher(remoteStateConfig)
        assertThat(newStateDispatcher.messagesQueue).hasSize(1)
    }

    @Test
    fun `When dispatching two in a row, only dispatch the first one`() {
        val remoteStateDispatcher = createRemoteStateDispatcher(remoteStateConfig)
        coreAPI.sendUserStateResponse = { runTest { delay(1000) } }

        remoteStateDispatcher.dispatch("testKey", JsonPrimitive("testValue"), isUrgent = true, domain = RemoteStateEntry.Domain.PROJECT)
        Thread.sleep(500)
        remoteStateDispatcher.dispatch("testKey", JsonPrimitive("testValue"), isUrgent = true, domain = RemoteStateEntry.Domain.PROJECT)

        runTest {
            delay(3000)
            assertThat(coreAPI.sendUserStateCount).isEqualTo(2)
            delay(2000)
            assertThat(remoteStateDispatcher.messagesQueue).hasSize(0)
        }
    }

    @Test
    fun `When dispatching with failure, return to messages queue `() {
        val remoteStateDispatcher = createRemoteStateDispatcher(remoteStateConfig)
        coreAPI.sendUserStateResponse = { throw Error()}

        remoteStateDispatcher.dispatch("testKey", JsonPrimitive("testValue"), isUrgent = true, domain = RemoteStateEntry.Domain.PROJECT)
        runTest {
            delay(2000)
            assertThat(coreAPI.sendUserStateCount).isEqualTo(1)
            delay(2000)
            assertThat(remoteStateDispatcher.messagesQueue).hasSize(1)
        }
    }

    private fun createRemoteStateDispatcher(remoteStateConfig: CoreConfiguration.RemoteState) =
        ProjectRemoteStateDispatcher(
            localStorage,
            "project",
            coreAPI,
            signatureGenerator,
            remoteStateConfig,
            PersistedQueue(localStorage.app.core.appRemoteStateQueue),
            App.resolve<Json>(),
            testScope
        )
}
