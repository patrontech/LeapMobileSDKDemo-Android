package com.greencopper.core.deferredcommand

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.net.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.coroutines.CoroutineContext

public interface DeferredCommandService: LifecycleObserver {
    public suspend fun defer(state: DeferredCommandState)
}

public suspend fun DeferredCommandService.defer(key: DeferredCommandKey): Unit =
    defer(DeferredCommandState(key))

public suspend inline fun <reified T> DeferredCommandService.defer(
    key: DeferredCommandKey,
    state: T,
    json: Json = App.resolve()
): Unit = defer(DeferredCommandState.create(key, state, json))

/**
 * The `ConcreteDeferredCommandService` implements a state
 * machine using [LocalStorage]. It reads a `Set` of
 * [DeferredCommandState] instances, each of which has a
 * key of type [DeferredCommandKey]. This `key` tells the
 * service which [DeferredCommand] to execute.
 *
 * The service passes the state to the command in the `execute`
 * method, which returns a `Set` of [DeferredCommandState] instances.
 *
 * If the command succeeds, or if it fails in such a way that it cannot
 * be run again, it will return an empty `Set`. If it needs to be run
 * again, it will return a new [DeferredCommandState] in the `Set`.
 *
 * In theory, a command can return more than one new state, or a state
 * for a different command altogether. For example, we could have a
 * command that schedules another command to be executed if it succeeds
 * (or if it fails or whatever we wish).
 */
internal class ConcreteDeferredCommandService(
    commands: List<DeferredCommand>,
    private val localStorage: LocalStorage,
    private val logger: Logging,
    networkMonitor: NetworkMonitor,
    lifecycleOwner: LifecycleOwner,
    scope: CoroutineScope,
    private val lock: CoroutineContext,
    /**
     * A coroutine semaphore for testing to avoid
     * the use of [delay].
     */
    internal var testSemaphore: Semaphore? = null
): DefaultLifecycleObserver, DeferredCommandService {
    private val commands = commands.associateBy { it.key }

    /**
     * Changing the value to a new random UUID ultimately forces [run]
     * to be called unless we're not connected.
     */
    private val nextStateFlow = MutableStateFlow(UUID.randomUUID())

    init {
        scope.launch {
            nextStateFlow
                .combine(networkMonitor.connectedFlow) { id, connected ->
                    id to connected
                }
                .distinctUntilChanged()
                .collectLatest { (_, connected) ->
                    try {
                        if (connected) run()
                    } finally {
                        testSemaphore?.release()
                        testSemaphore = null // To avoid over-releasing
                    }
                }
        }
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override suspend fun defer(state: DeferredCommandState) {
        withContext(lock) {
            val states = localStorage.app.core.deferredCommand.states.value.toMutableSet()
            states.add(state)
            localStorage.app.core.deferredCommand.states.value = states
        }
        nextStateFlow.value = UUID.randomUUID()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        nextStateFlow.value = UUID.randomUUID()
    }

    private suspend fun run() = withContext(lock) {
        val states = localStorage.app.core.deferredCommand.states.value
        val nextStates = mutableSetOf<DeferredCommandState>()
        for (state in states) {
            commands[state.key]?.let { command ->
                try {
                    nextStates.addAll(command.execute(state))
                } catch (e: Exception) {
                    /*
                    Yes, we do nothing. A well-implemented command
                    should never throw an exception, but this is
                    a catch-all.

                    The end result is that if a command throws an
                    exception, it will not be run again.
                     */
                    logger.e(
                        "DeferredCommand ${state.key} threw an exception: ${e.message}.",
                        throwable = e
                    )
                }
            }
        }
        localStorage.app.core.deferredCommand.states.value = nextStates
    }
}
