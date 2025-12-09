package com.greencopper.core.remotestate

import kotlinx.serialization.json.JsonElement

public class CustomRemoteStateEntry(
    key: String,
    value: JsonElement?,
    internal val container: String,
    isUrgent: Boolean
) : RemoteStateEntry(key, value, Domain.PROJECT, isUrgent)