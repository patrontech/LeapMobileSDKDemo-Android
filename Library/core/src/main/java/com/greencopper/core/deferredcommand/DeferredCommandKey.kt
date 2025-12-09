package com.greencopper.core.deferredcommand

import kotlinx.serialization.Serializable

@Serializable
public data class DeferredCommandKey(public val key: String) {
    public companion object;
}