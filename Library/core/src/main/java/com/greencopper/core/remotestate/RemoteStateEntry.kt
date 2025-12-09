package com.greencopper.core.remotestate

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public open class RemoteStateEntry(
    public val key: String,
    internal val value: JsonElement?,
    internal val domain: Domain = Domain.PROJECT,
    internal val isUrgent: Boolean,
) {
    public enum class Domain {
        APP, PROJECT
    }

    override fun toString(): String = value.toString()
}

public class NullRemoteStateEntry(key: String, isUrgent: Boolean = false) :
    RemoteStateEntry(key, null, Domain.PROJECT, isUrgent)
