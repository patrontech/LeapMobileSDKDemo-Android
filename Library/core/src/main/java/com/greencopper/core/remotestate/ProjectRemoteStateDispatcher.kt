package com.greencopper.core.remotestate

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.networking.CoreAPI
import com.greencopper.core.networking.SignatureGenerator
import com.greencopper.core.recipe.CoreConfiguration
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.d
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.encodeToJsonElement

internal class ProjectRemoteStateDispatcher(
    localStorage: LocalStorage,
    private val projectTag: String,
    private val coreAPI: CoreAPI,
    private val signatureGenerator: SignatureGenerator,
    private val remoteState: CoreConfiguration.RemoteState?,
    private val appRemoteStateQueue: PersistedQueue,
    private val json: Json,
    scope: CoroutineScope,
) {

    internal var messagesQueue =
        PersistedQueue(localStorage.project.core.remoteState.messages)
    private var dispatchesQueue =
        PersistedQueue(localStorage.project.core.remoteState.dispatches)
    private var dispatching: Mutex = Mutex()
    private val apiKey = "a70c6cf1ca40d32a00c0f0558608e3b6f82e79b9c3c89c2a740e06dbf0d63dee"
    private val dispatchChannel = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    private val remoteStateCustom = localStorage.project.core.remoteState.custom

    init {
        scope.launch {
            dispatchChannel.consumeEach { task ->
                task()
            }
        }
    }

    internal suspend fun dispatchRemoteState() {
        // We don't want to execute this method if
        // we don't have the RemoteState config, even
        // though it's not directly referenced here.
        // See the explanation in dispatchUpdateMap.
        remoteState ?: return
        dispatching.lock()
        try {
            transferMessagesToDispatches()
            if (dispatchesQueue.isEmpty() && appRemoteStateQueue.isEmpty()) {
                dispatching.unlockIfPossible()
                return
            }
            val updateMap = mutableMapOf<String, JsonElement?>()
            dispatchesQueue
                .plus(appRemoteStateQueue)
                .forEach { value ->
                    updateMap[value.key] = value.value
                }
            dispatchUpdateMap(updateMap)
        } catch (t: Throwable) {
            dispatching.unlockIfPossible()
        }
    }

    private suspend fun dispatchUpdateMap(updateMap: Map<String, JsonElement?>) {
        // If we don't have the remote state configuration,
        // we can't send the messages. This happens only in a rare edge case:
        // We're sending a message for a previous project that isn't currently
        // loaded and its remote state config hasn't been saved in LS because
        // it hasn't been loaded since this code change occurred. However,
        // we still want this ProjectRemoteStateDispatcher to be able to
        // store messages in its queue, in the event that the user switches
        // to it and it gets a config.
        //
        // In other words, this isn't a big deal and won't happen in normal
        // operations.
        val remoteState = remoteState ?: return
        try {
            coreAPI.sendUserState(
                url = remoteState.apiUrl,
                authHeader = signatureGenerator.getAuthenticationKey(projectTag = projectTag, apiKey = apiKey),
                body = updateMap,
            )

            dispatchesQueue.clear()
            dispatchesQueue.save()
            dispatching.unlockIfPossible()
            App.log.d("Successfully updated user state for project $projectTag")
        } catch (t: Throwable) {
            synchronized(messagesQueue) {
                val reformedQueue = dispatchesQueue + messagesQueue
                messagesQueue.clear()
                messagesQueue.addAll(reformedQueue)
                messagesQueue.save()
                dispatchesQueue.clear()
                dispatchesQueue.save()
            }
            App.log.e(
                message = "The remote state couldn't be sent to the CMS",
                throwable = t
            )
            dispatching.unlockIfPossible()
        }
    }

    private fun transferMessagesToDispatches() {
        synchronized(messagesQueue) {
            if (messagesQueue.isEmpty()) {
                dispatching.unlockIfPossible()
                return
            }
            var value = messagesQueue.poll()
            while (value != null) {
                dispatchesQueue.add(value)
                value = messagesQueue.poll()
            }
            dispatchesQueue.save()
            messagesQueue.removeAll(dispatchesQueue)
            messagesQueue.save()
        }
    }

    @Suppress("NAME_SHADOWING")
    fun dispatch(entry: RemoteStateEntry) {
        var entry = entry
        if (entry is CustomRemoteStateEntry) {
            val custom = remoteStateCustom.value
            custom[entry.key, entry.container] = entry.value ?: JsonNull
            remoteStateCustom.value = custom
            entry = RemoteStateEntry("custom", custom.toJson(), entry.domain, entry.isUrgent)
        }

        if (entry.domain == RemoteStateEntry.Domain.APP) {
            appRemoteStateQueue.add(entry)
            appRemoteStateQueue.save()
        } else {
            messagesQueue.add(entry)
            messagesQueue.save()
        }

        // See the explanation above in dispatchUpdateMap.
        remoteState?.let { remoteState ->
            if (entry.isUrgent || messagesQueue.size > remoteState.threshold) {
                dispatchChannel.trySend {
                    dispatchRemoteState()
                }
            }
        }
    }

    fun dispatch(
        key: String,
        value: JsonElement,
        domain: RemoteStateEntry.Domain,
        isUrgent: Boolean,
    ) {
        dispatch(RemoteStateEntry(key, value, domain, isUrgent))
    }

    inline fun <reified T> dispatch(
        key: String,
        value: T,
        domain: RemoteStateEntry.Domain,
        isUrgent: Boolean,
    ) {
        dispatch(key, json.encodeToJsonElement(value), domain, isUrgent)
    }

    inline fun <reified T> dispatch(
        key: String,
        value: T,
        container: String,
        isUrgent: Boolean,
    ) {
        dispatch(
            CustomRemoteStateEntry(
                key,
                json.encodeToJsonElement(value),
                container,
                isUrgent
            ),
        )
    }

    private fun Mutex.unlockIfPossible() {
        if (isLocked) unlock()
    }
}
