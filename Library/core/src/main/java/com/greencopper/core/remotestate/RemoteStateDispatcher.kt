package com.greencopper.core.remotestate

import androidx.lifecycle.LifecycleObserver
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

public interface RemoteStateDispatcher : LifecycleObserver {
    public fun dispatch(entry: RemoteStateEntry, project: String? = null)
    public val json: Json
}

public fun RemoteStateDispatcher.delete(
    key: String,
    isUrgent: Boolean,
    project: String? = null,
) {
    dispatch(NullRemoteStateEntry(key, isUrgent), project)
}

public fun RemoteStateDispatcher.dispatch(
    key: String,
    value: JsonElement,
    domain: RemoteStateEntry.Domain,
    isUrgent: Boolean,
    project: String? = null,
) {
    dispatch(RemoteStateEntry(key, value, domain, isUrgent), project)
}

public inline fun <reified T> RemoteStateDispatcher.dispatch(
    key: String,
    value: T,
    domain: RemoteStateEntry.Domain,
    isUrgent: Boolean,
    project: String? = null,
) {
    dispatch(key, json.encodeToJsonElement(value), domain, isUrgent, project)
}

/**
 * A container is a group of related keys, such as "disney".
 *
 * You *must* coordinate with the iOS team to be sure that you
 * use exactly the same keys and containers on both platforms.
 * Further, the serialization format of any serialized value
 * must be identical on both platforms.
 *
 * Example:
 *
 * ```kotlin
 * rsd.dispatchCustom("myID", "xyz-123", "disney", true)
 * ```
 */
public inline fun <reified T> RemoteStateDispatcher.dispatchCustom(
    key: String,
    value: T,
    container: String,
    isUrgent: Boolean,
    project: String? = null,
) {
    dispatch(
        CustomRemoteStateEntry(
            key,
            json.encodeToJsonElement(value),
            container,
            isUrgent
        ),
        project,
    )
}