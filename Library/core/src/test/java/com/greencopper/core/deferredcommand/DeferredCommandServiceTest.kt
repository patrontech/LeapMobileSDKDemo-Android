package com.greencopper.core.deferredcommand

import androidx.lifecycle.Lifecycle
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.TestLocalStorageContainer
import com.greencopper.core.localstorage.core
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.MockLifecycleOwner
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.testmocks.toolkit.MockNetworkMonitor
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext

internal class DeferredCommandServiceTest: CoroutineTest(StandardTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private class TestDeferredCommand: DeferredCommand {
        var executed: Boolean = false
        override val key: DeferredCommandKey = DeferredCommandKey.test

        override suspend fun execute(state: DeferredCommandState): Set<DeferredCommandState> {
            executed = true
            return state.get<Boolean>()?.let {
                if (it) {
                    emptySet()
                } else {
                    setOf(state)
                }
            } ?: run {
                emptySet()
            }
        }
    }

    private val networkMonitor = MockNetworkMonitor(false)
    private val lifecycleOwner = MockLifecycleOwner()
    private val container = TestLocalStorageContainer()
    private val localStorage = LocalStorage("test", container)
    private val command = TestDeferredCommand()
    private var testSemaphore = Semaphore(1, 1)

    override fun afterEach() {}

    @Test
    fun afterCallingDefer_stateIsAddedToLocalStorage() = runTest {
        val job = launch {
            val service = createService(this, dispatcher)
            service.defer(DeferredCommandKey.test, state = true)
            service.defer(DeferredCommandKey.test)
        }

        testSemaphore.acquire()
        assertThat(command.executed).isFalse
        assertThat(localStorage.app.core.deferredCommand.states.value).isNotEmpty

        job.cancel()
    }

    @Test
    fun whenConnectivityIsRestored_commandsAreExecuted() = runTest {
        lateinit var service: ConcreteDeferredCommandService
        val job = launch {
            service = createService(this, dispatcher)
            service.defer(DeferredCommandKey.test, state = true)
        }

        testSemaphore.acquire()
        assertThat(localStorage.app.core.deferredCommand.states.value).isNotEmpty

        testSemaphore = Semaphore(1, 1)
        service.testSemaphore = testSemaphore
        networkMonitor.mutableConnectedFlow.value = true

        testSemaphore.acquire()
        assertThat(command.executed).isTrue
        assertThat(localStorage.app.core.deferredCommand.states.value).isEmpty()

        job.cancel()
    }

    @Test
    fun whenCommandFails_stateIsAddedAgainToLocalStorage() = runTest {
        lateinit var service: ConcreteDeferredCommandService
        val job = launch {
            service = createService(this, dispatcher)
            service.defer(DeferredCommandKey.test, state = false)
        }

        testSemaphore.acquire()
        assertThat(localStorage.app.core.deferredCommand.states.value).isNotEmpty

        testSemaphore = Semaphore(1, 1)
        service.testSemaphore = testSemaphore
        networkMonitor.mutableConnectedFlow.value = true
        testSemaphore.acquire()
        assertThat(command.executed).isTrue
        assertThat(localStorage.app.core.deferredCommand.states.value).isNotEmpty

        job.cancel()
    }

    @Test
    fun whenAppEntersForeground_deferredCommandServiceRuns() = runTest {
        networkMonitor.mutableConnectedFlow.value = true

        val job = launch {
            val service = createService(this, dispatcher)
            service.defer(DeferredCommandKey.test, state = true)
        }
        lifecycleOwner.currentState = Lifecycle.State.STARTED

        testSemaphore.acquire()
        assertThat(command.executed).isTrue
        assertThat(localStorage.app.core.deferredCommand.states.value).isEmpty()

        job.cancel()
    }

    private fun createService(
        scope: CoroutineScope,
        lock: CoroutineContext
    ): ConcreteDeferredCommandService =
        ConcreteDeferredCommandService(
            listOf(command),
            localStorage,
            MockLogging(),
            networkMonitor,
            lifecycleOwner,
            scope,
            lock,
            testSemaphore
        )
}

private val DeferredCommandKey.Companion.test: DeferredCommandKey
    by lazy { DeferredCommandKey("Test.Test") }
